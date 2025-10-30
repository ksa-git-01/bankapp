package ru.yandex.practicum.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.frontui.dto.RatesResponse;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeClient {

    private final RestTemplate restTemplate;
    private static final String EXCHANGE_URL = "http://bankapp-exchange:8080";

    public List<RatesResponse> getRates() {
        log.debug("Getting rates from exchange service");
        String url = EXCHANGE_URL + "/api/rates";

        ResponseEntity<List<RatesResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RatesResponse>>() {}
        );

        return response.getBody();
    }
}