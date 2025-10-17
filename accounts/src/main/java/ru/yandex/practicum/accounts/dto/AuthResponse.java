package ru.yandex.practicum.accounts.dto;

public record AuthResponse(String token, String type, Long userId, String username, String role) {
}
