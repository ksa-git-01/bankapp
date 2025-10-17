package ru.yandex.practicum.accounts.service;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.accounts.configuration.RsaKeyProperties;
import ru.yandex.practicum.accounts.model.User;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final RsaKeyProperties keyProperties;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(keyProperties.privateKey(), Jwts.SIG.RS256)
                .compact();
    }
}
