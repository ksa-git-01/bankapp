package ru.yandex.practicum.cash.dto;

import java.math.BigDecimal;

public record AccountsRequest(Long userId, String currency, BigDecimal amount) {
}
