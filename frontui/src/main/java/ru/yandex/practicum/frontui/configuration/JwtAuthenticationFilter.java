package ru.yandex.practicum.frontui.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final RsaKeyProperties keyProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/actuator") ||
                path.equals("/login") ||
                path.equals("/error") ||
                path.equals("/signup")
                ;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        log.debug("=== JWT FILTER ===");
        log.debug("Request URI: {}", request.getRequestURI());

        String jwt = getJwtFromCookie(request);
        log.debug("JWT from cookie: {}", jwt != null ? "exists" : "null");

        if (jwt != null && !jwt.isEmpty()) {
            Claims claims = Jwts.parser()
                    .verifyWith(keyProperties.publicKey())
                    .build()
                    .parseSignedClaims(jwt)
                    .getBody();
            String username = claims.getSubject();
            log.debug("Username from JWT: {}", username);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class)))
                    );

            Map<String, Object> details = new HashMap<>();
            details.put("jwt", jwt);
            details.put("userId", claims.get("userId", Long.class));
            auth.setDetails(details);

            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Authentication set in SecurityContext");
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT-TOKEN".equals(cookie.getName())) {
                    String jwt = cookie.getValue();
                    return jwt;
                }
            }
        }
        return null;
    }
}