package ru.yandex.practicum.cash.dto;

public record AccountsResponse(boolean success, String message, Double balance) {
}

