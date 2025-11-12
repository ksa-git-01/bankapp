package ru.yandex.practicum.cash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.cash.dto.BlockerOperationCheckResponse;
import ru.yandex.practicum.cash.dto.CashOperationRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockerClient {

    private final RestTemplate restTemplate;

    private static final String BLOCKER_URL = "http://bankapp-blocker:8080";

    public boolean checkOperation(CashOperationRequest request) {
        log.debug("Checking operation with Blocker service");

        try {
            ResponseEntity<BlockerOperationCheckResponse> response = restTemplate.postForEntity(
                    BLOCKER_URL + "/api/blocker/check",
                    request,
                    BlockerOperationCheckResponse.class
            );

            return Boolean.TRUE.equals(response.getBody().blocked());

        } catch (Exception e) {
            log.error("Error calling Blocker service: {}", e.getMessage());
            // В случае ошибки в Blocker - разрешаем операцию
            return false;
        }
    }
}