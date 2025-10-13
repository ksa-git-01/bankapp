package ru.yandex.practicum.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RegistrationRequest {
    private String login;
    private String password;
    private String confirmPassword;
    private String name;
    private String email;
    private LocalDate birthdate;
}