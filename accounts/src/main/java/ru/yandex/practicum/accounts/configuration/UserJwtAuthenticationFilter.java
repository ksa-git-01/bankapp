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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userJwt = request.getHeader("X-User-JWT");

        if (userJwt != null && !userJwt.isEmpty()) {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(rsaKeyProperties.publicKey())
                        .build()
                        .parseSignedClaims(userJwt)
                        .getBody();

                String username = claims.getSubject();
                Long userId = claims.get("userId", Long.class);
                String role = claims.get("role", String.class);

                log.debug("User JWT validated: username={}, userId={}, role={}",
                        username, userId, role);

                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("userRole", role);

            } catch (Exception e) {
                log.warn("Invalid user JWT: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}