package ru.yandex.practicum.notifications.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.notifications.dto.NotificationRequest;
import ru.yandex.practicum.notifications.dto.NotificationResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class NotificationService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public NotificationResponse sendNotification(NotificationRequest request) {
        String timestamp = LocalDateTime.now().format(formatter);

        log.info("╔═════════════");
        log.info("║ NOTIFICATION");
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
}