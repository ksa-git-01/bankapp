package ru.yandex.practicum.exchange.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("exchange_history")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExchangeHistory {
    @Id
    private Long id;
    private Long userId;
    private String currencyFrom;
    private BigDecimal amountFrom;
    private String currencyTo;
    private BigDecimal amountTo;
    private BigDecimal ratio;
    private LocalDateTime createdAt;
}
