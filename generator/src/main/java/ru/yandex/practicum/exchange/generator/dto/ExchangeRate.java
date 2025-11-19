package ru.yandex.practicum.exchange.generator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {
    private String currencyFrom;
    private String currencyTo;
    private BigDecimal ratio;
}
