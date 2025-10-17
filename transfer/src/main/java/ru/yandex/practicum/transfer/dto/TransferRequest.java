package ru.yandex.practicum.transfer.dto;

public record TransferRequest(Long fromUserId,
                              Long toUserId,
                              String fromCurrency,
                              String toCurrency,
                              Double amount) {
}