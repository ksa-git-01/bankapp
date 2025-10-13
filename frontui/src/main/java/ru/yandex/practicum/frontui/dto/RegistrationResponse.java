package ru.yandex.practicum.frontui.dto;

import lombok.*;

@Data
public class RegistrationResponse {
    private final Long userId;
    private final String username;
    private final String message;
}