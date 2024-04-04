package edu.eflerrr.bot.configuration.kafka;

import edu.eflerrr.bot.configuration.ApplicationConfig;
import java.util.HashMap;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "add-queue", havingValue = "true")
public class KafkaDLQTopicConfig {

    private final String bootstrapAddress;
    private final String dqlTopicName;

    @Autowired
    public KafkaDLQTopicConfig(ApplicationConfig config) {
        this.bootstrapAddress = config.kafka().bootstrapServers();
        this.dqlTopicName = config.kafka().dlq().dlqTopicName();
    }

    @Bean
    public KafkaAdmin kafkaAdminBean() {
        return new KafkaAdmin(new HashMap<>() {{
            put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        }});
    }

    @Bean
    public NewTopic dlqTopicBean() {
        return new NewTopic(dqlTopicName, 1, (short) 1);
    }

}
