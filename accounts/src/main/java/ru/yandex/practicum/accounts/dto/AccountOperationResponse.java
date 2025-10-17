package ru.yandex.practicum.accounts.dto;

public record AccountOperationResponse(boolean success, String message, Double balance) {
}