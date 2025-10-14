package ru.yandex.practicum.accounts.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private final String password;
    private final String confirmPassword;
}