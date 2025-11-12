package ru.yandex.practicum.accounts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.accounts.configuration.RsaKeyProperties;
import ru.yandex.practicum.accounts.configuration.TestContainersConfiguration;
import ru.yandex.practicum.accounts.dto.CreateAccountRequest;
import ru.yandex.practicum.accounts.dto.DepositRequest;
import ru.yandex.practicum.accounts.dto.WithdrawRequest;

import java.math.BigDecimal;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RsaKeyProperties rsaKeyProperties;

    private String userJwtToken;
    private RSAPrivateKey testPrivateKey;

    @BeforeEach
    void setUp() {
        testPrivateKey = rsaKeyProperties.privateKey();
        userJwtToken = generateUserJwt(1L);
    }

    @Test
    void getUserAccountsWithValidTokenSuccess() throws Exception {
        mockMvc.perform(get("/api/accounts/user/1")
                        .header("X-User-JWT", userJwtToken)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getUserAccountsWithoutUserJwt() throws Exception {
        mockMvc.perform(get("/api/accounts/user/1")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserAccountsWithUserIdMismatch() throws Exception {
        mockMvc.perform(get("/api/accounts/user/2")
                        .header("X-User-JWT", userJwtToken)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    void createAccountWithValidDataSuccess() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(1L, "GBP");

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-JWT", userJwtToken)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.balance").value(0.0));
    }

    @Test
    void depositWithValidDataSuccess() throws Exception {
        DepositRequest request = new DepositRequest(1L, "RUB", BigDecimal.valueOf(1000.0));

        mockMvc.perform(post("/api/accounts/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.balance").isNumber());
    }

    @Test
    void depositWithNegativeAmount() throws Exception {
        DepositRequest request = new DepositRequest(1L, "RUB", BigDecimal.valueOf(-100));

        mockMvc.perform(post("/api/accounts/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void withdrawWithValidDataSuccess() throws Exception {
        WithdrawRequest request = new WithdrawRequest(1L, "RUB", BigDecimal.valueOf(100.0));

        mockMvc.perform(post("/api/accounts/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.balance").isNumber());
    }

    @Test
    void withdrawWithInsufficientFunds() throws Exception {
        WithdrawRequest request = new WithdrawRequest(1L, "RUB", BigDecimal.valueOf(999999.0));

        mockMvc.perform(post("/api/accounts/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteAccountWithValidDataSuccess() throws Exception {
        CreateAccountRequest createRequest = new CreateAccountRequest(1L, "USDT");
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .header("X-User-JWT", userJwtToken)
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                ));

        mockMvc.perform(delete("/api/accounts/user/1/currency/USDT")
                        .header("X-User-JWT", userJwtToken)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
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