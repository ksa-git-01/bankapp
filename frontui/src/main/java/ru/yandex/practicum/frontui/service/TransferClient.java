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
public class TransferClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TRANSFER_URL = "http://bankapp-transfer";

    public void transfer(Long fromUserId, Long toUserId,
                         String fromCurrency, String toCurrency,
                         Double amount) {
        log.debug("Processing transfer: from user {} to user {}, amount {} {}",
                fromUserId, toUserId, amount, fromCurrency);

        Map<String, Object> request = Map.of(
                "fromUserId", fromUserId,
                "toUserId", toUserId,
                "fromCurrency", fromCurrency,
                "toCurrency", toCurrency,
                "amount", amount
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    TRANSFER_URL + "/api/transfer",
                    request,
                    Map.class
            );

            log.debug("Transfer successful");
            response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Transfer failed: {}", e.getResponseBodyAsString());
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