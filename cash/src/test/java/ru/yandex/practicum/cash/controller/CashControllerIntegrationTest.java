package ru.yandex.practicum.cash.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.cash.configuration.RsaKeyProperties;
import ru.yandex.practicum.cash.dto.CashOperationRequest;
import ru.yandex.practicum.cash.dto.CashOperationResponse;
import ru.yandex.practicum.cash.exception.OperationBlockedException;
import ru.yandex.practicum.cash.service.CashService;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CashControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CashService cashService;

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

    private String userJwtToken;

    @BeforeEach
    void setUp() {
        userJwtToken = generateUserJwt(1L);
    }

    @Test
    void cashOperationSuccess() throws Exception {
        CashOperationRequest request = new CashOperationRequest(1L, "DEPOSIT", "RUB", 1000.0);
        CashOperationResponse response = new CashOperationResponse(true, "Operation completed successfully", 5000.0);

        when(cashService.processCashOperation(any(CashOperationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/cash/operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-JWT", userJwtToken)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_cash-access"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Operation completed successfully"))
                .andExpect(jsonPath("$.newBalance").value(5000.0));
    }

    @Test
    void cashOperationWithoutOAuth2Token() throws Exception {
        CashOperationRequest request = new CashOperationRequest(1L, "DEPOSIT", "RUB", 1000.0);

        mockMvc.perform(post("/api/cash/operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-JWT", userJwtToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cashOperationWithoutUserJwtToken() throws Exception {
        CashOperationRequest request = new CashOperationRequest(1L, "DEPOSIT", "RUB", 1000.0);

        mockMvc.perform(post("/api/cash/operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_cash-access"))
                        ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void cashOperationWhenUserIdMismatch() throws Exception {
        CashOperationRequest request = new CashOperationRequest(2L, "DEPOSIT", "RUB", 1000.0);
        String anotherUserJwt = generateUserJwt(1L);

        mockMvc.perform(post("/api/cash/operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-JWT", anotherUserJwt)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_cash-access"))
                        ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void cashOperationWithoutRequiredRole() throws Exception {
        CashOperationRequest request = new CashOperationRequest(1L, "DEPOSIT", "RUB", 1000.0);

        mockMvc.perform(post("/api/cash/operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-JWT", userJwtToken)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_other-role"))
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    void cashOperationWhenOperationBlocked() throws Exception {
        CashOperationRequest request = new CashOperationRequest(1L, "WITHDRAW", "RUB", 200000.0);

        when(cashService.processCashOperation(any(CashOperationRequest.class)))
                .thenThrow(new OperationBlockedException("Operation blocked due to suspicious activity"));

        mockMvc.perform(post("/api/cash/operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-JWT", userJwtToken)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_cash-access"))
                        ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Operation blocked due to suspicious activity"));
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
}