package ru.yandex.practicum.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AuthResponse {
    private String token;
    private String type;
    private Long userId;
    private String username;
    private String role;
}
