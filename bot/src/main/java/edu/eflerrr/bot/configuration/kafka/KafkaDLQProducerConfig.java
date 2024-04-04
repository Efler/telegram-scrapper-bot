package edu.eflerrr.bot.configuration.kafka;

import edu.eflerrr.bot.configuration.ApplicationConfig;
import java.util.HashMap;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "add-queue", havingValue = "true")
public class KafkaDLQProducerConfig {

    private final String bootstrapAddress;
    private final String clientId;

    @Autowired
    public KafkaDLQProducerConfig(ApplicationConfig config) {
        this.bootstrapAddress = config.kafka().bootstrapServers();
        this.clientId = config.kafka().consumer().clientId();
    }

    @Bean
    public ProducerFactory<String, byte[]> dlqProducerFactoryBean() {
        return new DefaultKafkaProducerFactory<>(new HashMap<>() {
            {
                put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
                put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
                put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
            }
        }, new StringSerializer(), new ByteArraySerializer());
    }

    @Bean
    public KafkaTemplate<String, byte[]> dlqKafkaTemplateBean() {
        return new KafkaTemplate<>(dlqProducerFactoryBean());
    }

}
