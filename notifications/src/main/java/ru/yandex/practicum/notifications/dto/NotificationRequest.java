package ru.yandex.practicum.notifications.dto;

public record NotificationRequest(Long userId, String type, String message, Double amount, String currency) {
}