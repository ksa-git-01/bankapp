package ru.yandex.practicum.transfer.dto;

import java.math.BigDecimal;

public record ExchangeRate(String currencyFrom, String currencyTo, BigDecimal ratio) {
}
