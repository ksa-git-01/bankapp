package ru.yandex.practicum.transfer.dto;

public record AccountsRequest(Long userId, String currency, Double amount) {
}
