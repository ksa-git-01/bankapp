package ru.yandex.practicum.cash.dto;

import java.math.BigDecimal;

public record CashOperationResponse(boolean success, String message, BigDecimal newBalance) {
}