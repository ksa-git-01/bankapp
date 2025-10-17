package ru.yandex.practicum.accounts.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "accounts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Account {
    @Id
    private Long id;
    private Long userId;
    private String currency; // RUB, USD, EUR
    private Double balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}