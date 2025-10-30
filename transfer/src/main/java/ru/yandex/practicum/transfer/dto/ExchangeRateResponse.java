package ru.yandex.practicum.transfer.dto;

import java.math.BigDecimal;

public record ExchangeRateResponse(String currencyFrom, String currencyTo, BigDecimal ratio) {
}
