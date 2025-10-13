package ru.yandex.practicum.accounts.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table("users")
@Data
public class User {

    @Id
    private Long id;
    private String username;
    private String password;
    private String name;
    private String email;
    private LocalDate birthdate;
    private String role;
    private Boolean enabled;
    private LocalDateTime createdAt;
}