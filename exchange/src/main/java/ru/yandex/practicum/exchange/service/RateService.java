package ru.yandex.practicum.exchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exchange.model.Rate;
import ru.yandex.practicum.exchange.repository.RateRepository;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RateService {
    private final RateRepository rateRepository;

    public void createRate(String currencyFrom, String currencyTo, BigDecimal ratio){
        Optional<Rate> rate = rateRepository.findByCurrencyFromAndCurrencyTo(currencyFrom, currencyTo);
        rate.ifPresentOrElse(r -> {
            r.setRatio(ratio);
            rateRepository.save(r);
            },
                () -> {
                    Rate newRate = new Rate();
                    newRate.setCurrencyFrom(currencyFrom);
                    newRate.setCurrencyTo(currencyTo);
                    newRate.setRatio(ratio);
                    rateRepository.save(newRate);
                });
    }
}
