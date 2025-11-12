package ru.yandex.practicum.cash.dto;

import java.math.BigDecimal;

public record AccountsResponse(boolean success, String message, BigDecimal balance) {
}

