package ru.yandex.practicum.frontui.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private final String password;
    private final String confirmPassword;
}