package ru.yandex.practicum.transfer.dto;

import java.math.BigDecimal;

public record TransferRequest(Long fromUserId,
                              Long toUserId,
                              String fromCurrency,
                              String toCurrency,
                              BigDecimal amount) {
}