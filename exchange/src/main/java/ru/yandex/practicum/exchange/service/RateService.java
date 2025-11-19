package ru.yandex.practicum.exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exchange.dto.ExchangeRate;
import ru.yandex.practicum.exchange.dto.RatesResponse;
import ru.yandex.practicum.exchange.model.Rate;
import ru.yandex.practicum.exchange.repository.RateRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateService {
    private final RateRepository rateRepository;

    public void createRate(ExchangeRate rate){
        log.debug("Creating rate : {} {} {}", rate.getCurrencyFrom(), rate.getCurrencyTo(), rate.getRatio());
        Optional<Rate> currentRate = rateRepository.findByCurrencyFromAndCurrencyTo(rate.getCurrencyFrom(), rate.getCurrencyTo());
        currentRate.ifPresentOrElse(r -> {
            r.setRatio(rate.getRatio());
            rateRepository.save(r);
            },
                () -> {
                    Rate newRate = new Rate();
                    newRate.setCurrencyFrom(rate.getCurrencyFrom());
                    newRate.setCurrencyTo(rate.getCurrencyTo());
                    newRate.setRatio(rate.getRatio());
                    rateRepository.save(newRate);
                });
    }

    public List<RatesResponse> getRates() {
        List<RatesResponse> rateList = new ArrayList<>();
        rateRepository.findAll().forEach(rate -> rateList.add(new RatesResponse(rate.getCurrencyFrom(), rate.getCurrencyTo(), rate.getRatio())));
        return rateList;
    }
}
