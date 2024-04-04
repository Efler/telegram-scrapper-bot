package edu.eflerrr.bot.service.kafka;

import edu.eflerrr.bot.controller.dto.request.LinkUpdate;
import edu.eflerrr.bot.service.UpdatesService;
import java.net.MalformedURLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app", name = "add-queue", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class BotQueueConsumer {

    private final UpdatesService updatesService;

    @KafkaListener(
        topics = "${app.kafka.topic.name}",
        groupId = "${app.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(LinkUpdate message) throws MalformedURLException {
        log.debug("[BotQueueConsumer] Successfully consuming message: " + message);
        updatesService.processUpdate(
            message.getId(),
            message.getUrl().toURL(),
            message.getDescription(),
            message.getTgChatIds()
        );
    }

    @KafkaListener(
        topics = "${app.kafka.dlq.dlq-topic-name}",
        groupId = "${app.kafka.consumer.group-id}",
        containerFactory = "dlqKafkaListenerContainerFactory"
    )
    public void consumeDLQ(String message) {
        log.error("[BotQueueConsumer - DLQ] Invalid message from DLQ: " + message);
    }

}
