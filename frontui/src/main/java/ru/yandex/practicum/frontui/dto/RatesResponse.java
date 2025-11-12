package ru.yandex.practicum.frontui.dto;

public record RatesResponse(
        Long id,
        String currencyFrom,
        String currencyTo,
        Double ratio
) {}