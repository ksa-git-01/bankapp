package ru.yandex.practicum.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DepositRequest {
    private Long userId;
    private String currency;
    private BigDecimal amount;
}