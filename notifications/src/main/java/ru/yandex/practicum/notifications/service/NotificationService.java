package ru.yandex.practicum.notifications.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.notifications.dto.NotificationEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class NotificationService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

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
    }
}