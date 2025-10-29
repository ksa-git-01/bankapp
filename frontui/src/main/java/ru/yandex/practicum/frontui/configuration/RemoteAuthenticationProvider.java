package ru.yandex.practicum.frontui.configuration;

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

    private static final String ACCOUNTS_URL = "http://bankapp-accounts";

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        AuthRequest request = new AuthRequest(username, password);
        log.debug("AuthRequest: {}", request);
        String url = ACCOUNTS_URL + "/accounts/api/auth";

        AuthResponse response;
        try {
            response = restTemplate.postForObject(url, request, AuthResponse.class);
            log.debug("AuthResponse: {}", response);
        } catch (Exception e) {
            throw new BadCredentialsException("Authentication failed: " + e.getMessage());
        }

        if (response.token() == null) {
            throw new BadCredentialsException("Invalid credentials");
        }

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + response.role()))
                );

        Map<String, Object> details = new HashMap<>();
        details.put("jwt", response.token());
        details.put("userId", response.userId());
        details.put("role", response.role());
        auth.setDetails(details);

        return auth;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}