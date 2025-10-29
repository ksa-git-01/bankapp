package ru.yandex.practicum.exchange.generator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.exchange.generator.dto.RatesRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeClient {
    private final RestTemplate restTemplate;

    private static final String EXCHANGE_URL = "http://bankapp-exchange";

    public void createRate(RatesRequest request) {
        log.debug("Filling rates in Exchange service");

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    EXCHANGE_URL + "/api/rates",
                    request,
                    Void.class
            );
        } catch (Exception e) {
            log.error("Error calling Exchange service: {}", e.getMessage());
        }
    }
}
