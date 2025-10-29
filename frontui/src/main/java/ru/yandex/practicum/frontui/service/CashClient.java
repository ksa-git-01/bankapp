package ru.yandex.practicum.frontui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CASH_URL = "http://bankapp-cash";

    public void processCashOperation(Long userId, String operation, String currency, Double amount) {
        log.debug("Processing cash operation: user={}, operation={}, currency={}, amount={}",
                userId, operation, currency, amount);

        Map<String, Object> request = Map.of(
                "userId", userId,
                "operation", operation,
                "currency", currency,
                "amount", amount
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    CASH_URL + "/api/cash/operation",
                    request,
                    Map.class
            );

            log.debug("Cash operation successful");
            response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Cash operation failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException(extractErrorMessage(e));
        }
    }

    private String extractErrorMessage(HttpClientErrorException e) {
        try {
            String responseBody = e.getResponseBodyAsString();
            @SuppressWarnings("unchecked")
            Map<String, Object> errorBody = objectMapper.readValue(responseBody, Map.class);

            String message = (String) errorBody.get("message");
            if (message != null) {
                return message;
            }
        } catch (Exception ex) {
            log.warn("Failed to parse error response: {}", ex.getMessage());
        }

        return e.getMessage();
    }
}