package ru.yandex.practicum.accounts.dto;

import java.math.BigDecimal;

public record AccountDto(Long id, String currency, BigDecimal balance) {
}