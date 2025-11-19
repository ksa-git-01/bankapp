package ru.yandex.practicum.notifications.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.notifications.dto.NotificationEvent;
import ru.yandex.practicum.notifications.dto.NotificationRequest;
import ru.yandex.practicum.notifications.dto.NotificationResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class NotificationService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // Старый метод для HTTP API (если нужен)
    public NotificationResponse sendNotification(NotificationRequest request) {
        String timestamp = LocalDateTime.now().format(formatter);

        log.info("╔═════════════");
        log.info("║ NOTIFICATION (HTTP)");
        log.info("╠═════════════");
        log.info("║ Time: {}", timestamp);
        log.info("║ User ID: {}", request.userId());
        log.info("║ Type: {}", request.type());

        if (request.amount() != null && request.currency() != null) {
            log.info("║ Amount:{} {}",
                    request.amount(),
                    request.currency());
        }

        log.info("║ Message:  {}", request.message());
        log.info("╚═════════════");

        return new NotificationResponse(true, "Notification sent successfully");
    }

    // Новый метод для обработки Kafka событий
    public void processNotificationEvent(NotificationEvent event) {
        String timestamp = event.getTimestamp() != null
                ? event.getTimestamp().format(formatter)
                : LocalDateTime.now().format(formatter);

        log.info("╔═════════════════════════");
        log.info("║ NOTIFICATION (KAFKA)");
        log.info("╠═════════════════════════");
        log.info("║ Event ID: {}", event.getEventId());
        log.info("║ Time: {}", timestamp);
        log.info("║ User ID: {}", event.getUserId());
        log.info("║ Type: {}", event.getEventType());
        log.info("║ Message: {}", event.getMessage());
        log.info("╚═════════════════════════");

        // Здесь можно добавить реальную отправку:
        // - Email
        // - SMS
        // - Push notifications
        // - Сохранение в БД
    }
}