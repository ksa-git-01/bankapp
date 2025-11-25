package ru.yandex.practicum.cash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.cash.dto.NotificationEvent;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${kafka.topics.notifications}")
    private String notificationsTopic;

    public void sendNotification(Long userId, String eventType, String message) {
        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .userId(userId)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            CompletableFuture<SendResult<String, NotificationEvent>> future =
                    kafkaTemplate.send(notificationsTopic, userId.toString(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Sent notification event: {} to topic: {} partition: {}",
                            event.getEventId(),
                            notificationsTopic,
                            result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to send notification event: {}", event.getEventId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error sending notification to Kafka", e);
        }
    }
}
