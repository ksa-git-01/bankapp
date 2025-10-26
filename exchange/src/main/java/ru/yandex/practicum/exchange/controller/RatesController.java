package ru.yandex.practicum.exchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.exchange.service.RateService;

import java.math.BigDecimal;

@RestController("/api/rates")
@RequiredArgsConstructor
public class RatesController {
    private final RateService rateService;

    @PostMapping
    public ResponseEntity<Void> createRate(@RequestParam String currencyFrom,
                           @RequestParam String currencyTo,
                           @RequestParam BigDecimal ratio){
        try {
            rateService.createRate(currencyFrom, currencyTo, ratio);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }
}
