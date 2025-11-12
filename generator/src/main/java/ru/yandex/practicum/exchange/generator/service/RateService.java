package ru.yandex.practicum.exchange.generator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exchange.generator.dto.RatesRequest;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class RateService {
    private final ExchangeClient exchangeClient;

    public void generateRates() {
        double rubToUsd = ThreadLocalRandom.current().nextDouble(60, 100);
        exchangeClient.createRate(new RatesRequest("RUB", "USD", BigDecimal.valueOf(rubToUsd)));

        double usdToRub = rubToUsd - 5;
        exchangeClient.createRate(new RatesRequest("USD", "RUB", BigDecimal.valueOf(usdToRub)));

        double rubToEur = ThreadLocalRandom.current().nextDouble(60, 100);
        exchangeClient.createRate(new RatesRequest("RUB", "EUR", BigDecimal.valueOf(rubToEur)));

        double eurToRub = rubToEur - 5;
        exchangeClient.createRate(new RatesRequest("EUR", "RUB", BigDecimal.valueOf(eurToRub)));
    }
}
