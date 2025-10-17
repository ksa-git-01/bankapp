package ru.yandex.practicum.cash.exception;

public class OperationBlockedException extends RuntimeException {
    public OperationBlockedException(String message) {
        super(message);
    }
}