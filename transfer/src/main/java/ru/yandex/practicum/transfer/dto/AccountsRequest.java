package ru.yandex.practicum.transfer.dto;

import java.math.BigDecimal;

public record AccountsRequest(Long userId, String currency, BigDecimal amount) {
}
