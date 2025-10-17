package ru.yandex.practicum.frontui.dto;

import java.time.LocalDate;

public record UpdateUserInfoRequest(String name, String email, LocalDate birthdate) {
}