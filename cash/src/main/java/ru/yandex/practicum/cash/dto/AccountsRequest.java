package ru.yandex.practicum.cash.dto;

public record AccountsRequest(Long userId, String currency, Double amount) {
}
