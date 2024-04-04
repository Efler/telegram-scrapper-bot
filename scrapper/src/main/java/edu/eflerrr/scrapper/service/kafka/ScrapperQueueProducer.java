package edu.eflerrr.scrapper.service.kafka;

import edu.eflerrr.scrapper.client.dto.request.SendUpdateRequest;
import edu.eflerrr.scrapper.configuration.ApplicationConfig;
import edu.eflerrr.scrapper.service.UpdateSender;
import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "true")
@Slf4j
public class ScrapperQueueProducer implements UpdateSender {

    private final KafkaTemplate<String, SendUpdateRequest> kafkaTemplate;
    private final String topicName;

    @Autowired
    public ScrapperQueueProducer(
        KafkaTemplate<String, SendUpdateRequest> kafkaTemplate, ApplicationConfig config
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = config.kafka().topic().name();
    }

    @Override
    public void sendUpdate(Long id, URI url, String description, List<Long> tgChatIds) {
        var message = new SendUpdateRequest(id, url, description, tgChatIds);
        var future = kafkaTemplate.send(
            topicName, message
        );
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug(
                    "[ScrapperQueueProducer] Successfully sending message, message: "
                        + message
                        + ", offset in partition: "
                        + result.getRecordMetadata().offset()
                );
            } else {
                log.debug("[ScrapperQueueProducer] Failed to send message! message: "
                    + message
                    + "reason: "
                    + ex.getMessage());
            }
        });
    }

}
