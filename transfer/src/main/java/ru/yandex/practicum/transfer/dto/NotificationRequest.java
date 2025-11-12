package ru.yandex.practicum.transfer.dto;

import java.math.BigDecimal;

public record NotificationRequest(Long userId, String type, String message, BigDecimal amount, String currency) {
}
