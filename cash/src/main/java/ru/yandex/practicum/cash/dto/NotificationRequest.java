package ru.yandex.practicum.cash.dto;

public record NotificationRequest(Long userId, String type, String message, Double amount, String currency) {
}
