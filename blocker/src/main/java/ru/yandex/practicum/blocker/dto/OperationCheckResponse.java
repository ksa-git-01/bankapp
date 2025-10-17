package ru.yandex.practicum.blocker.dto;

public record OperationCheckResponse(boolean blocked, String reason) {
}