package ru.yandex.practicum.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.frontui.dto.AuthRequest;
import ru.yandex.practicum.frontui.dto.AuthResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final RestTemplate restTemplate;

    public AuthResponse authenticate(String username, String password) {
        AuthRequest request = new AuthRequest(username, password);
        log.debug("AuthRequest: {}", request);
        AuthResponse response = restTemplate.postForObject("/accounts/api/auth", request, AuthResponse.class);
        log.debug("AuthResponse: {}", response);
        return response;
    }


}
