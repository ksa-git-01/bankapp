package ru.yandex.practicum.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.notifications.dto.NotificationEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${kafka.topics.notifications}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotification(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received notification event from partition {} offset {}: eventId={}, eventType={}",
                partition, offset, event.getEventId(), event.getEventType());

        try {
            notificationService.processNotificationEvent(event);
            log.info("Successfully processed notification event: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to process notification event: {}", event.getEventId(), e);
            throw e;
        }
    }
}
