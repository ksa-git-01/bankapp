package ru.yandex.practicum.exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exchange.dto.ExchangeHistoryRequest;
import ru.yandex.practicum.exchange.model.ExchangeHistory;
import ru.yandex.practicum.exchange.repository.ExchangeRepository;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {
    private final ExchangeRepository exchangeRepository;

    public void saveHistory(ExchangeHistoryRequest request) {
        ExchangeHistory history = new ExchangeHistory();
        history.setUserIdFrom(request.userIdFrom());
        history.setCurrencyFrom(request.currencyFrom());
        history.setAmountFrom(request.amountFrom());
        history.setUserIdTo(request.userIdTo());
        history.setCurrencyTo(request.currencyTo());
        history.setAmountTo(request.amountTo());
        history.setCreatedAt(LocalDateTime.now());

        exchangeRepository.save(history);
        log.debug("Exchange history saved for user {}", request.userIdFrom());
    }
}
