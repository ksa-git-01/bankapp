package ru.yandex.practicum.frontui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.frontui.service.ExchangeClient;
import ru.yandex.practicum.frontui.dto.RatesResponse;

import java.util.List;

@RestController
@RequestMapping("/api/rates")
@RequiredArgsConstructor
@Slf4j
public class RatesController {

    private final ExchangeClient exchangeClient;

    @GetMapping
    public ResponseEntity<List<RatesResponse>> getRates() {
        log.debug("Fetching rates");
        try {
            List<RatesResponse> rates = exchangeClient.getRates();
            return ResponseEntity.ok(rates);
        } catch (Exception e) {
            log.error("Failed to fetch rates", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}