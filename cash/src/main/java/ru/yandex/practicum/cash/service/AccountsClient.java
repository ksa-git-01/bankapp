package ru.yandex.practicum.cash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.cash.dto.AccountsRequest;
import ru.yandex.practicum.cash.dto.AccountsResponse;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountsClient {

    private final RestTemplate restTemplate;

    public Double deposit(Long userId, String currency, Double amount) {
        log.debug("Depositing {} {} to user {}", amount, currency, userId);

        AccountsRequest request = new AccountsRequest(userId, currency, amount);

        try {
            ResponseEntity<AccountsResponse> response = restTemplate.postForEntity(
                    "/accounts/api/accounts/deposit",
                    request,
                    AccountsResponse.class
            );

            return response.getBody().balance();

        } catch (HttpClientErrorException e) {
            log.error("Deposit failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Ошибка пополнения счета: " + e.getMessage());
        }
    }

    public Double withdraw(Long userId, String currency, Double amount) {
        log.debug("Withdrawing {} {} from user {}", amount, currency, userId);

        AccountsRequest request = new AccountsRequest(userId, currency, amount);

        try {
            ResponseEntity<AccountsResponse> response = restTemplate.postForEntity(
                    "/accounts/api/accounts/withdraw",
                    request,
                    AccountsResponse.class
            );

            return response.getBody().balance();

        } catch (HttpClientErrorException e) {
            log.error("Withdraw failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Ошибка снятия со счета: " + e.getMessage());
        }
    }
}