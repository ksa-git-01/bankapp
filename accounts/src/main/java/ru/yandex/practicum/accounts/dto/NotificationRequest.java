package ru.yandex.practicum.accounts.dto;

public record NotificationRequest(Long userId, String type, String message) {
}
