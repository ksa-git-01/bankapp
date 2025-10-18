package ru.yandex.practicum.frontui.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.frontui.configuration.RsaKeyProperties;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CookieIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static RSAPrivateKey testPrivateKey;
    private static RSAPublicKey testPublicKey;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public RsaKeyProperties testRsaKeyProperties() throws Exception {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            testPublicKey = (RSAPublicKey) keyPair.getPublic();
            testPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

            return new RsaKeyProperties(testPublicKey, testPrivateKey);
        }
    }

    @Test
    void jwtCookieCanSetAndCanBeRetrieved() throws Exception {
        String jwt = generateUserJwt(1L);
        Cookie jwtCookie = new Cookie("JWT-TOKEN", jwt);

        MvcResult result = mockMvc.perform(get("/login")
                        .cookie(jwtCookie))
                .andReturn();

        Cookie[] cookies = result.getRequest().getCookies();
        assertNotNull(cookies);

        Cookie retrievedCookie = findCookie(cookies, "JWT-TOKEN");
        assertNotNull(retrievedCookie);
        assertEquals(jwt, retrievedCookie.getValue());
    }

    @Test
    void jwtCookieCorrectProcessingByJwtAuthenticationFilter() throws Exception {
        String jwt = generateUserJwt(1L);

        Cookie jwtCookie = new Cookie("JWT-TOKEN", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");

        mockMvc.perform(get("/login")
                        .cookie(jwtCookie))
                .andExpect(status().isOk());
    }

    private String generateUserJwt(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 3600000);

        return Jwts.builder()
                .setSubject("user" + userId)
                .claim("userId", userId)
                .claim("role", "USER")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(testPrivateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    private Cookie findCookie(Cookie[] cookies, String name) {
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }
}