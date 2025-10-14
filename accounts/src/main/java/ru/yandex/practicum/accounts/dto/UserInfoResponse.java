package ru.yandex.practicum.accounts.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserInfoResponse {
    private final Long id;
    private final String username;
    private final String name;
    private final String email;
    private final LocalDate birthdate;
    private final String role;
}