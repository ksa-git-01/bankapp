package ru.yandex.practicum.accounts.dto;

import java.math.BigDecimal;

public record AccountOperationResponse(boolean success, String message, BigDecimal balance) {
}