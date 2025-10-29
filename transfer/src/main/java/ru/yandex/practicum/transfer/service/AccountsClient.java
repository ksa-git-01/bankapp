package ru.yandex.practicum.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.transfer.dto.AccountsRequest;
import ru.yandex.practicum.transfer.dto.AccountsResponse;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountsClient {

    private final RestTemplate restTemplate;

    private static final String ACCOUNTS_URL = "http://bankapp-accounts";

    public BigDecimal deposit(Long userId, String currency, BigDecimal amount) {
        log.debug("Depositing {} {} to user {}", amount, currency, userId);

        AccountsRequest request = new AccountsRequest(userId, currency, amount);

        try {
            ResponseEntity<AccountsResponse> response = restTemplate.postForEntity(
                    ACCOUNTS_URL+ "/api/accounts/deposit",
                    request,
                    AccountsResponse.class
            );

            return response.getBody().balance();

        } catch (HttpClientErrorException e) {
            log.error("Deposit failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Ошибка пополнения счета: " + e.getMessage());
        }
    }

    public BigDecimal withdraw(Long userId, String currency, BigDecimal amount) {
        log.debug("Withdrawing {} {} from user {}", amount, currency, userId);

        AccountsRequest request = new AccountsRequest(userId, currency, amount);

        try {
            ResponseEntity<AccountsResponse> response = restTemplate.postForEntity(
                    ACCOUNTS_URL + "/api/accounts/withdraw",
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