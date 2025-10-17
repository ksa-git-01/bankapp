package ru.yandex.practicum.transfer.dto;

public record NotificationRequest(Long userId, String type, String message, Double amount, String currency) {
}
