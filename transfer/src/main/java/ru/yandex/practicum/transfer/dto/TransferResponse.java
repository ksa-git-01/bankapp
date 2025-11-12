package ru.yandex.practicum.transfer.dto;

import java.math.BigDecimal;

public record TransferResponse(boolean success,
                               String message,
                               BigDecimal fromBalance,
                               BigDecimal toBalance) {
}