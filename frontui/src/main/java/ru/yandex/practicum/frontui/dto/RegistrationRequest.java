package ru.yandex.practicum.frontui.dto;

import java.time.LocalDate;

public record RegistrationRequest(String login, String password, String confirmPassword, String name, String email,
                                  LocalDate birthdate) {
}