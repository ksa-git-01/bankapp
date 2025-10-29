package ru.yandex.practicum.frontui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
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
@RequiredArgsConstructor
@Slf4j
public class AccountsClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String ACCOUNTS_URL = "http://bankapp-accounts";

    public void registerUser(RegistrationRequest request) {
        log.debug("Registering user via accounts service: {}", request.login());

        try {
            ResponseEntity<RegistrationResponse> response = restTemplate.postForEntity(
                    ACCOUNTS_URL + "/api/registration",
                    request,
                    RegistrationResponse.class
            );

            log.debug("User registered successfully: {}", request.login());
            response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Registration failed: {}", e.getResponseBodyAsString());
            throw new RegistrationException(extractErrorMessage(e));
        }
    }

    public UserInfoResponse getUserInfoByUserId(Long userId) {
        log.debug("Getting user info for userId: {}", userId);

        try {
            ResponseEntity<UserInfoResponse> response = restTemplate.getForEntity(
                    ACCOUNTS_URL + "/accounts/api/users/id/" + userId,
                    UserInfoResponse.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            throw new AccountsServiceException(extractErrorMessage(e));
        }
    }

    public UserInfoResponse getUserInfoByUsername(String username) {
        log.debug("Getting user info for username: {}", username);

        try {
            ResponseEntity<UserInfoResponse> response = restTemplate.getForEntity(
                    ACCOUNTS_URL + "/api/users/username/" + username,
                    UserInfoResponse.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            throw new AccountsServiceException(extractErrorMessage(e));
        }
    }

    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        log.debug("Updating password for userId: {}", userId);

        try {
            restTemplate.put(
                    ACCOUNTS_URL + "/api/users/" + userId + "/password",
                    request
            );

            log.debug("Password updated successfully");

        } catch (HttpClientErrorException e) {
            log.error("Failed to update password: {}", e.getResponseBodyAsString());
            throw new AccountsServiceException(extractErrorMessage(e));
        }
    }

    public void updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        log.debug("Updating user info for userId: {}", userId);

        try {
            ResponseEntity<UserInfoResponse> response = restTemplate.exchange(
                    ACCOUNTS_URL + "/api/users/" + userId,
                    HttpMethod.PUT,
                    new org.springframework.http.HttpEntity<>(request),
                    UserInfoResponse.class
            );

            log.debug("User info updated successfully");
            response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Failed to update user info: {}", e.getResponseBodyAsString());
            throw new AccountsServiceException(extractErrorMessage(e));
        }
    }

    public List<AccountDto> getUserAccounts(Long userId) {
        log.debug("Getting accounts for userId: {}", userId);

        try {
            ResponseEntity<List<AccountDto>> response = restTemplate.exchange(
                    ACCOUNTS_URL + "/api/accounts/user/" + userId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Failed to get user accounts: {}", e.getResponseBodyAsString());
            throw new AccountsServiceException(extractErrorMessage(e));
        }
    }

    public void createAccount(Long userId, String currency) {
        log.debug("Creating account: userId={}, currency={}", userId, currency);

        CreateAccountRequest request = new CreateAccountRequest(userId, currency);

        try {
            ResponseEntity<AccountDto> response = restTemplate.postForEntity(
                    ACCOUNTS_URL + "/api/accounts",
                    request,
                    AccountDto.class
            );

            log.debug("Account created successfully");
            response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Failed to create account: {}", e.getResponseBodyAsString());
            throw new AccountsServiceException(extractErrorMessage(e));
        }
    }

    public void deleteAccount(Long userId, String currency) {
        log.debug("Deleting account: userId={}, currency={}", userId, currency);

        try {
            restTemplate.delete(ACCOUNTS_URL + "/api/accounts/user/" + userId + "/currency/" + currency);
            log.debug("Account deleted successfully");

        } catch (HttpClientErrorException e) {
            log.error("Failed to delete account: {}", e.getResponseBodyAsString());
            throw new AccountsServiceException(extractErrorMessage(e));
        }
    }

    public List<UserListDto> getAllUsers() {
        log.debug("Getting all users list");

        try {
            ResponseEntity<List<UserListDto>> response = restTemplate.exchange(
                    ACCOUNTS_URL + "/api/users",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Failed to get users list: {}", e.getResponseBodyAsString());
            return List.of();
        }
    }

    private String extractErrorMessage(HttpClientErrorException e) {
        try {
            String responseBody = e.getResponseBodyAsString();

            @SuppressWarnings("unchecked")
            Map<String, Object> errorBody = objectMapper.readValue(responseBody, Map.class);

            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) errorBody.get("errors");

            if (errors != null && !errors.isEmpty()) {
                return String.join("; ", errors);
            }

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