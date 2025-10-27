package ru.yandex.practicum.exchange.generator.dto;

import java.math.BigDecimal;

public record RatesRequest(String currencyFrom, String currencyTo, BigDecimal ratio) {
}
