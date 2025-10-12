package ru.yandex.practicum.frontui.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.frontui.configuration.security.RsaKeyProperties;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final RsaKeyProperties keyProperties;

    public boolean validateToken(String token) {
        try {
            log.info("=== VALIDATING JWT ===");

            Jwts.parser()
                    .verifyWith(keyProperties.publicKey())
                    .build()
                    .parseSignedClaims(token);

            log.info("JWT validation SUCCESS");
            return true;
        } catch (JwtException e) {
            log.info("JWT validation FAILED: {}", e.getClass().getName());
            log.info("Error message: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.info("JWT validation FAILED: IllegalArgumentException");
            log.info("Error message: {}", e.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(keyProperties.publicKey())
                .build()
                .parseSignedClaims(token)
                .getBody();
    }
}
