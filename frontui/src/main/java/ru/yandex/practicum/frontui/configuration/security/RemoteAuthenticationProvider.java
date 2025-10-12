package ru.yandex.practicum.frontui.configuration.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.frontui.dto.AuthRequest;
import ru.yandex.practicum.frontui.dto.AuthResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RemoteAuthenticationProvider implements AuthenticationProvider {

    private final RestTemplate restTemplate;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        log.info("=== REMOTE AUTH PROVIDER ===");
        log.info("Username: {}", username);

        AuthRequest request = new AuthRequest(username, password);
        String url = "http://gateway/accounts/api/auth";

        AuthResponse response;
        try {
            response = restTemplate.postForObject(url, request, AuthResponse.class);
            log.info("Response from accounts: {}", response);
            log.info("JWT token: {}", response.getToken());
        } catch (Exception e) {
            log.info("Error calling accounts: {}", e.getMessage());
            throw new BadCredentialsException("Authentication failed: " + e.getMessage());
        }

        if (response.getToken() == null) {
            throw new BadCredentialsException("Invalid credentials");
        }

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + response.getRole()))
                );

        Map<String, Object> details = new HashMap<>();
        details.put("jwt", response.getToken());
        details.put("userId", response.getUserId());
        details.put("role", response.getRole());
        auth.setDetails(details);

        log.info("Authentication created with JWT in details");

        return auth;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}