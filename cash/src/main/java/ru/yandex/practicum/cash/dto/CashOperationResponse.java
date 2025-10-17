package ru.yandex.practicum.cash.dto;

public record CashOperationResponse(boolean success, String message, Double newBalance) {
}