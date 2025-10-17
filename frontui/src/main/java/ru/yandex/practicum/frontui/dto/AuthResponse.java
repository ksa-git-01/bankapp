package ru.yandex.practicum.frontui.dto;

public record AuthResponse(String token, String type, Long userId, String username, String role) {
}
