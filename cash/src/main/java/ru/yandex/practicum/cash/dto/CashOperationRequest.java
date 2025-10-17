package ru.yandex.practicum.cash.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CashOperationRequest {
    private Long userId;
    private String operation; // "DEPOSIT" или "WITHDRAW"
    private String currency;
    private Double amount;
}