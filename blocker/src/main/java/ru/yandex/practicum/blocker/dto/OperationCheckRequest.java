package ru.yandex.practicum.blocker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OperationCheckRequest {
    private Long userId;
    private String operation; // "DEPOSIT" или "WITHDRAW"
    private String currency;
    private BigDecimal amount;
}