package ru.yandex.practicum.exchange.dto;

import java.math.BigDecimal;

public record RatesResponse(String currencyFrom, String currencyTo, BigDecimal ratio){}
