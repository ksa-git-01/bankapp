package ru.yandex.practicum.exchange.repository;

import org.springframework.data.repository.CrudRepository;
import ru.yandex.practicum.exchange.model.Rate;

import java.util.Optional;

public interface RateRepository extends CrudRepository<Rate, Long> {
    Optional<Rate> findByCurrencyFromAndCurrencyTo(String currencyFrom, String currencyTo);
}
