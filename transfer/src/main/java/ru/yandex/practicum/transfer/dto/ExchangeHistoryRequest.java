package ru.yandex.practicum.transfer.dto;

import java.math.BigDecimal;

public record ExchangeHistoryRequest(Long userIdFrom,
                                     String currencyFrom,
                                     BigDecimal amountFrom,
                                     Long userIdTo,
                                     String currencyTo,
                                     BigDecimal amountTo) {
}
