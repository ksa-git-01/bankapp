package ru.yandex.practicum.cash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.cash.dto.NotificationRequest;
import ru.yandex.practicum.cash.dto.NotificationResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationsClient {

    private final RestTemplate restTemplate;

    public void sendNotification(Long userId, String type, String message,
                                 Double amount, String currency) {
        log.debug("Sending notification to user {}: {}", userId, type);

        NotificationRequest request = new NotificationRequest(userId, type, message, amount, currency);

        try {
            restTemplate.postForEntity(
                    "/notifications/api/notifications/send",
                    request,
                    NotificationResponse.class
            );
            log.debug("Notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }
}