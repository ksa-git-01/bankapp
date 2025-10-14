package ru.yandex.practicum.frontui.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserInfoRequest {
    private final String name;
    private final String email;
    private final LocalDate birthdate;
}