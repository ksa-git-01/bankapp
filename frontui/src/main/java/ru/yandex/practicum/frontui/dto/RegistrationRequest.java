package ru.yandex.practicum.frontui.dto;

import lombok.*;

import java.time.LocalDate;

@Data
public class RegistrationRequest {
    private final String login;
    private final String password;
    private final String confirmPassword;
    private final String name;
    private final String email;
    private final LocalDate birthdate;
}