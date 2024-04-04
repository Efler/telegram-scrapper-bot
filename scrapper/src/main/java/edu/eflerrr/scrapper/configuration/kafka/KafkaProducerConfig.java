package edu.eflerrr.scrapper.configuration.kafka;

import edu.eflerrr.scrapper.client.dto.request.SendUpdateRequest;
import edu.eflerrr.scrapper.configuration.ApplicationConfig;
import java.util.HashMap;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "true")
public class KafkaProducerConfig {

    private final String bootstrapAddress;
    private final String clientId;
    private final String acks;

    @Autowired
    public KafkaProducerConfig(ApplicationConfig config) {
        this.bootstrapAddress = config.kafka().bootstrapServers();
        this.clientId = config.kafka().producer().clientId();
        this.acks = config.kafka().producer().acks();
    }

    @Bean
    public ProducerFactory<String, SendUpdateRequest> producerFactoryBean() {
        return new DefaultKafkaProducerFactory<>(new HashMap<>() {
            {
                put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
                put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
                put(ProducerConfig.ACKS_CONFIG, acks);
                put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            }
        });
    }

    @Bean
    public KafkaTemplate<String, SendUpdateRequest> kafkaTemplateBean() {
        return new KafkaTemplate<>(producerFactoryBean());
    }

}
