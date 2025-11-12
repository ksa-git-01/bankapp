package ru.yandex.practicum.exchange.repository;

import org.springframework.data.repository.CrudRepository;
import ru.yandex.practicum.exchange.model.ExchangeHistory;

public interface ExchangeRepository extends CrudRepository<ExchangeHistory, Long> {
}
