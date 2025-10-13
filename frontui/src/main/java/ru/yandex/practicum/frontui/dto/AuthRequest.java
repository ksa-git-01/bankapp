package ru.yandex.practicum.frontui.dto;

import lombok.*;

@Data
public class AuthRequest {
    private final String username;
    private final String password;
}
