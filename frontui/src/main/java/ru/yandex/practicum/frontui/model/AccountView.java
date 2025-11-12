package ru.yandex.practicum.frontui.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.yandex.practicum.frontui.dto.AccountDto;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class AccountView {
    private Currency currency;
    private boolean exists;
    private BigDecimal value;

    public static AccountView fromDto(AccountDto dto) {
        return new AccountView(
                Currency.valueOf(dto.currency()),
                true,
                dto.balance()
        );
    }

    public static AccountView empty(Currency currency) {
        return new AccountView(currency, false, BigDecimal.ZERO);
    }
}