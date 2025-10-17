package ru.yandex.practicum.transfer.configuration;

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

        String userJwt = request.getHeader("X-User-JWT");

        if (userJwt != null && !userJwt.isEmpty()) {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(rsaKeyProperties.publicKey())
                        .build()
                        .parseSignedClaims(userJwt)
                        .getBody();
                //Помещаем информацию о пользователе из токена в HttpServletRequest
                request.setAttribute("userId", claims.get("userId", Long.class));
            } catch (Exception e) {
                log.warn("User JWT validation failed: {}", e.getMessage());
            }
        } else {
            log.info("No User JWT in request (X-User-JWT header)");
        }
        filterChain.doFilter(request, response);
    }
}