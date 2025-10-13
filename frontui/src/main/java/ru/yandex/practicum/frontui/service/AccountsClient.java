package ru.yandex.practicum.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.frontui.dto.RegistrationRequest;
import ru.yandex.practicum.frontui.dto.RegistrationResponse;
import ru.yandex.practicum.frontui.exception.RegistrationException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountsClient {

    // Тот же RestTemplate с OAuth2
    private final RestTemplate restTemplate;
    @Value("${accounts.service.url}")
    private String accountsUrl;

    public RegistrationResponse registerUser(RegistrationRequest request) {
        log.info("Registering user via accounts service: {}", request.getLogin());

        try {
            ResponseEntity<RegistrationResponse> response = restTemplate.postForEntity(
                    accountsUrl + "/api/registration",
                    request,
                    RegistrationResponse.class
            );

            log.info("User registered successfully: {}", request.getLogin());
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Registration failed: {}", e.getResponseBodyAsString());
            throw new RegistrationException("Registration failed: " + e.getMessage());
        }
    }
}