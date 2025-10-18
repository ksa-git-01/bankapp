package ru.yandex.practicum.frontui.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.frontui.configuration.RsaKeyProperties;
import ru.yandex.practicum.frontui.dto.AccountDto;
import ru.yandex.practicum.frontui.dto.UserInfoResponse;
import ru.yandex.practicum.frontui.dto.UserListDto;
import ru.yandex.practicum.frontui.exception.AccountsServiceException;
import ru.yandex.practicum.frontui.service.AccountsClient;
import ru.yandex.practicum.frontui.service.CashClient;
import ru.yandex.practicum.frontui.service.TransferClient;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountsClient accountsClient;

    @MockitoBean
    private CashClient cashClient;

    @MockitoBean
    private TransferClient transferClient;

    private static RSAPrivateKey testPrivateKey;
    private static RSAPublicKey testPublicKey;

    private String userJwtToken;

    @BeforeEach
    void setUp() {
        userJwtToken = generateUserJwt(1L);
    }

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
    void mainPageWithAuthenticatedUserReturnsMainView() throws Exception {
        UserInfoResponse userInfo = new UserInfoResponse(
                1L, "testuser", "Test User", "test@yandex.ru", LocalDate.of(1990, 1, 1), "USER"
        );
        List<AccountDto> accounts = List.of(
                new AccountDto(1L, "RUB", 1000.0),
                new AccountDto(2L, "EUR", 500.0)
        );
        List<UserListDto> users = List.of(
                new UserListDto(2L, "user2", "Second User")
        );

        when(accountsClient.getUserInfoByUserId(1L)).thenReturn(userInfo);
        when(accountsClient.getUserAccounts(1L)).thenReturn(accounts);
        when(accountsClient.getAllUsers()).thenReturn(users);

        Cookie jwtCookie = new Cookie("JWT-TOKEN", userJwtToken);

        mockMvc.perform(get("/")
                        .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("login", "user1"))
                .andExpect(model().attribute("userId", 1L))
                .andExpect(model().attribute("name", "Test User"))
                .andExpect(model().attribute("email", "test@yandex.ru"))
                .andExpect(model().attributeExists("accounts"))
                .andExpect(model().attributeExists("currency"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    void editPasswordWithValidDataRedirectsToMainPage() throws Exception {
        Cookie jwtCookie = new Cookie("JWT-TOKEN", userJwtToken);

        mockMvc.perform(post("/user/user1/editPassword")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .cookie(jwtCookie)
                        .param("password", "newPassword123")
                        .param("confirm_password", "newPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(accountsClient).updatePassword(eq(1L), any());
    }

    @Test
    void editUserInfoWithValidDataRedirectsToMainPage() throws Exception {
        when(accountsClient.getUserInfoByUserId(1L)).thenReturn(
                new UserInfoResponse(1L, "user1", "Test User", "test@yandex.ru", LocalDate.of(1990, 1, 1), "USER")
        );
        when(accountsClient.getUserAccounts(1L)).thenReturn(List.of());

        Cookie jwtCookie = new Cookie("JWT-TOKEN", userJwtToken);

        mockMvc.perform(post("/user/user1/editUserAccounts")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .cookie(jwtCookie)
                        .param("name", "Updated Name")
                        .param("email", "updated@yandex.ru")
                        .param("birthdate", "1990-05-15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(accountsClient).updateUserInfo(eq(1L), any());
    }

    @Test
    void processCashDeposit() throws Exception {
        Cookie jwtCookie = new Cookie("JWT-TOKEN", userJwtToken);

        mockMvc.perform(post("/user/user1/cash")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .cookie(jwtCookie)
                        .param("currency", "RUB")
                        .param("value", "1000.0")
                        .param("action", "PUT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(cashClient).processCashOperation(1L, "DEPOSIT", "RUB", 1000.0);
    }

    @Test
    void processCashWithdraw() throws Exception {
        Cookie jwtCookie = new Cookie("JWT-TOKEN", userJwtToken);

        mockMvc.perform(post("/user/user1/cash")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .cookie(jwtCookie)
                        .param("currency", "RUB")
                        .param("value", "500.0")
                        .param("action", "GET"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(cashClient).processCashOperation(1L, "WITHDRAW", "RUB", 500.0);
    }

    @Test
    void processTransferToSelf() throws Exception {
        Cookie jwtCookie = new Cookie("JWT-TOKEN", userJwtToken);

        mockMvc.perform(post("/user/user1/transfer")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .cookie(jwtCookie)
                        .param("to_login", "user1")
                        .param("from_currency", "RUB")
                        .param("to_currency", "EUR")
                        .param("value", "100.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(transferClient).transfer(1L, 1L, "RUB", "EUR", 100.0);
    }

    @Test
    void processTransferToAnotherUser() throws Exception {
        when(accountsClient.getUserInfoByUsername("user2"))
                .thenReturn(new UserInfoResponse(2L, "user2", "Second User", "user2@yandex.ru", LocalDate.of(1995, 1, 1), "USER"));

        Cookie jwtCookie = new Cookie("JWT-TOKEN", userJwtToken);

        mockMvc.perform(post("/user/user1/transfer")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .cookie(jwtCookie)
                        .param("to_login", "user2")
                        .param("from_currency", "RUB")
                        .param("to_currency", "EUR")
                        .param("value", "100.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(transferClient).transfer(1L, 2L, "RUB", "EUR", 100.0);
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