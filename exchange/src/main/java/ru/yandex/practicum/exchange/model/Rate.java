package ru.yandex.practicum.exchange.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("exchange_rate")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Rate {
    @Id
    private Long id;
    private String currencyFrom;
    private String currencyTo;
    private BigDecimal ratio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
