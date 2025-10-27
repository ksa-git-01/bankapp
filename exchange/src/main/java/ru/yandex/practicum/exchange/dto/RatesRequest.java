package ru.yandex.practicum.exchange.dto;

import java.math.BigDecimal;

public record RatesRequest(String currencyFrom, String currencyTo, BigDecimal ratio) {
}