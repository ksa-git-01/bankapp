package ru.yandex.practicum.frontui.exception;

public class AccountsServiceException extends RuntimeException {
    public AccountsServiceException(String message) {
        super(message);
    }
}