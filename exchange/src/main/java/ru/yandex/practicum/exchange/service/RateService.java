package ru.yandex.practicum.exchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exchange.dto.RatesResponse;
import ru.yandex.practicum.exchange.model.Rate;
import ru.yandex.practicum.exchange.repository.RateRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

    public List<RatesResponse> getRates() {
        List<RatesResponse> rateList = new ArrayList<>();
        rateRepository.findAll().forEach(rate -> rateList.add(new RatesResponse(rate.getCurrencyFrom(), rate.getCurrencyTo(), rate.getRatio())));
        return rateList;
    }
}
