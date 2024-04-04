package edu.eflerrr.bot.configuration.kafka;

import edu.eflerrr.bot.configuration.ApplicationConfig;
import edu.eflerrr.bot.controller.dto.request.LinkUpdate;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "add-queue", havingValue = "true")
@EnableKafka
@Slf4j
public class KafkaConsumerConfig {

    private final KafkaTemplate<String, byte[]> dlqKafkaTemplate;
    private final String bootstrapAddress;
    private final String clientId;
    private final String groupId;
    private final String autoOffsetReset;
    private final String dlqTopicName;
    private final long backoffInterval;
    private final int backoffMaxAttempts;

    @Autowired
    public KafkaConsumerConfig(ApplicationConfig config, KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.dlqKafkaTemplate = kafkaTemplate;
        this.bootstrapAddress = config.kafka().bootstrapServers();
        this.clientId = config.kafka().consumer().clientId();
        this.groupId = config.kafka().consumer().groupId();
        this.autoOffsetReset = config.kafka().consumer().autoOffsetReset();
        this.dlqTopicName = config.kafka().dlq().dlqTopicName();
        this.backoffInterval = config.kafka().dlq().backoffInterval();
        this.backoffMaxAttempts = config.kafka().dlq().backoffMaxAttempts();
    }

    @Bean
    public ConsumerFactory<String, LinkUpdate> consumerFactory() {
        var deserializer = new JsonDeserializer<>(LinkUpdate.class);
        deserializer.configure(new HashMap<>() {{
            put(JsonDeserializer.TRUSTED_PACKAGES, "*");
            put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        }}, false);
        var errorHandlingDeserializer = new ErrorHandlingDeserializer<>(deserializer);
        return new DefaultKafkaConsumerFactory<>(new HashMap<>() {
            {
                put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
                put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
                put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
                put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
                put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
                put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
                put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
            }
        }, new StringDeserializer(), errorHandlingDeserializer);
    }

    @Bean
    public ConsumerFactory<String, String> dlqConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(new HashMap<>() {
            {
                put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
                put(ConsumerConfig.CLIENT_ID_CONFIG, "bot-dql-consumer");
                put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
                put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
                put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            }
        }, new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LinkUpdate>
    kafkaListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, LinkUpdate>();
        factory.setConsumerFactory(consumerFactory());

        var recoverer = new DeadLetterPublishingRecoverer(
            dlqKafkaTemplate,
            (r, e) -> {
                log.warn("[DeadLetterPublishingRecoverer] Failed to process message! Exception: " + e);
                return new TopicPartition(dlqTopicName, r.partition());
            }
        );
        var errorHandler = new DefaultErrorHandler(
            recoverer, new FixedBackOff(backoffInterval, backoffMaxAttempts)
        );
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    dlqKafkaListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(dlqConsumerFactory());

        return factory;
    }

}
