package ru.yandex.practicum.accounts.dto;

import java.time.LocalDate;

public record UserInfoResponse(Long id, String username, String name, String email, LocalDate birthdate, String role) {
}