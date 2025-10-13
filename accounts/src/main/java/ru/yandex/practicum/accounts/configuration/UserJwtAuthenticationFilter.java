package ru.yandex.practicum.accounts.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserJwtAuthenticationFilter extends OncePerRequestFilter {

    private final RsaKeyProperties rsaKeyProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        log.info("┌─────────────────────────────────────────────────");
        log.info("│ [ACCOUNTS] Incoming request: {} {}", request.getMethod(), request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String tokenPreview = token.substring(0, Math.min(50, token.length())) + "...";
            log.info("│ [ACCOUNTS] OAuth2 Bearer token: {}", tokenPreview);
            log.info("│   (will be validated by Spring OAuth2 Resource Server)");
        } else {
            log.warn("│ [ACCOUNTS] ✗ No OAuth2 Bearer token found!");
        }

        String userJwt = request.getHeader("X-User-JWT");

        if (userJwt != null && !userJwt.isEmpty()) {
            String jwtPreview = userJwt.substring(0, Math.min(50, userJwt.length())) + "...";
            log.info("│ [ACCOUNTS] User JWT found: {}", jwtPreview);

            try {
                log.info("│ [ACCOUNTS] Validating User JWT with RSA public key...");

                Claims claims = Jwts.parser()
                        .verifyWith(rsaKeyProperties.publicKey())
                        .build()
                        .parseSignedClaims(userJwt)
                        .getBody();

                String username = claims.getSubject();
                Long userId = claims.get("userId", Long.class);
                String role = claims.get("role", String.class);

                log.info("│ [ACCOUNTS] ✓ User JWT validated successfully");
                log.info("│   Username: {}", username);
                log.info("│   UserId: {}", userId);
                log.info("│   Role: {}", role);

                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("userRole", role);

            } catch (Exception e) {
                log.warn("│ [ACCOUNTS] ✗ User JWT validation failed: {}", e.getMessage());
            }
        } else {
            log.info("│ [ACCOUNTS] No User JWT in request (X-User-JWT header absent)");
        }

        log.info("└─────────────────────────────────────────────────");

        filterChain.doFilter(request, response);
    }
}