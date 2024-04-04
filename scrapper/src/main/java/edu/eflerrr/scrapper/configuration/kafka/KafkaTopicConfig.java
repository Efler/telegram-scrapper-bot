package edu.eflerrr.scrapper.configuration.kafka;

import edu.eflerrr.scrapper.configuration.ApplicationConfig;
import java.util.HashMap;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "true")
public class KafkaTopicConfig {

    private final String bootstrapAddress;
    private final String topicName;
    private final int topicPartitions;
    private final short topicReplicationFactor;

    @Autowired
    public KafkaTopicConfig(ApplicationConfig config) {
        this.bootstrapAddress = config.kafka().bootstrapServers();
        this.topicName = config.kafka().topic().name();
        this.topicPartitions = config.kafka().topic().partitions();
        this.topicReplicationFactor = config.kafka().topic().replicationFactor();
    }

    @Bean
    public KafkaAdmin kafkaAdminBean() {
        return new KafkaAdmin(new HashMap<>() {{
            put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        }});
    }

    @Bean
    public NewTopic topicBean() {
        return new NewTopic(topicName, topicPartitions, topicReplicationFactor);
    }

}
