package ru.yandex.practicum.frontui.dto;

public record UpdatePasswordRequest(String password, String confirmPassword) {
}