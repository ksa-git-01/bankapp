package ru.yandex.practicum.frontui.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.frontui.dto.AuthRequest;
import ru.yandex.practicum.frontui.dto.AuthResponse;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public AuthResponse authenticate(String username, String password) {
        AuthRequest request = new AuthRequest(username, password);

        String url = gatewayUrl + "/accounts/api/auth";

        return restTemplate.postForObject(url, request, AuthResponse.class);
    }
}
