package ru.yandex.practicum.transfer.dto;

import java.math.BigDecimal;

public record AccountsResponse(boolean success, String message, BigDecimal balance) {
}

