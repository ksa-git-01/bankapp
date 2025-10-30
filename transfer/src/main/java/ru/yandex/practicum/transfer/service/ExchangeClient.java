package ru.yandex.practicum.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.transfer.dto.ExchangeHistoryRequest;
import ru.yandex.practicum.transfer.dto.ExchangeRateResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeClient {

    private final RestTemplate restTemplate;

    private static final String EXCHANGE_URL = "http://bankapp-exchange:8080";

    public ExchangeRateResponse getRates() {
        log.debug("Getting rates");

        try {
            ResponseEntity<ExchangeRateResponse> response = restTemplate.getForEntity(
                    EXCHANGE_URL + "/api/rates",
                    ExchangeRateResponse.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Getting rates failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Ошибка получения курсов обмена валют: " + e.getMessage());
        }
    }

    public void createConversionHistory(ExchangeHistoryRequest request) {
        log.debug("Creating conversion history record");

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    EXCHANGE_URL + "/api/exchange",
                    request,
                    Void.class
            );
        } catch (HttpClientErrorException e) {
            log.error("Creating conversion history record: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Ошибка записи истории трансфера: " + e.getMessage());
        }
    }
}
