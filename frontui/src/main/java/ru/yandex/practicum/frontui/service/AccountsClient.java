package ru.yandex.practicum.frontui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.frontui.dto.*;
import ru.yandex.practicum.frontui.exception.AccountsServiceException;
import ru.yandex.practicum.frontui.exception.RegistrationException;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AccountsClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${accounts.service.url}")
    private String accountsUrl;

    public AccountsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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
            throw new RegistrationException(extractErrorMessage(e));
        }
    }

    public UserInfoResponse getUserInfo(Long userId) {
        log.info("Getting user info for userId: {}", userId);

        try {
            ResponseEntity<UserInfoResponse> response = restTemplate.getForEntity(
                    accountsUrl + "/api/users/" + userId,
                    UserInfoResponse.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Failed to get user info: {}", e.getResponseBodyAsString());
            throw new AccountsServiceException(extractErrorMessage(e));
        }
    }

    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        log.info("Updating password for userId: {}", userId);

        try {
            restTemplate.put(
                    accountsUrl + "/api/users/" + userId + "/password",
                    request
            );

            log.info("Password updated successfully");

        } catch (HttpClientErrorException e) {
            log.error("Failed to update password: {}", e.getResponseBodyAsString());
            throw new AccountsServiceException(extractErrorMessage(e));
        }
    }

    public UserInfoResponse updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        log.info("Updating user info for userId: {}", userId);

        try {
            ResponseEntity<UserInfoResponse> response = restTemplate.exchange(
                    accountsUrl + "/api/users/" + userId,
                    HttpMethod.PUT,
                    new org.springframework.http.HttpEntity<>(request),
                    UserInfoResponse.class
            );

            log.info("User info updated successfully");
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Failed to update user info: {}", e.getResponseBodyAsString());
            throw new AccountsServiceException(extractErrorMessage(e));
        }
    }

    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);

        try {
            restTemplate.delete(accountsUrl + "/api/users/" + userId);
            log.info("User deleted successfully");

        } catch (HttpClientErrorException e) {
            log.error("Failed to delete user: {}", e.getResponseBodyAsString());
            throw new AccountsServiceException(extractErrorMessage(e));
        }
    }

    /**
     * Извлекает читаемое сообщение об ошибке из HTTP exception
     */
    private String extractErrorMessage(HttpClientErrorException e) {
        try {
            String responseBody = e.getResponseBodyAsString();

            @SuppressWarnings("unchecked")
            Map<String, Object> errorBody = objectMapper.readValue(responseBody, Map.class);

            // Пытаемся получить список ошибок
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) errorBody.get("errors");

            if (errors != null && !errors.isEmpty()) {
                return String.join("; ", errors);
            }

            // Если нет списка, берем message
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