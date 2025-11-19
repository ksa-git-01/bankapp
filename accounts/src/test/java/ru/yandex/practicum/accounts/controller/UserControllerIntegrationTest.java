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
import ru.yandex.practicum.accounts.dto.UpdatePasswordRequest;
import ru.yandex.practicum.accounts.dto.UpdateUserInfoRequest;

import java.security.interfaces.RSAPrivateKey;
import java.time.LocalDate;
import java.util.Date;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

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
    void getUserInfoByUserIdWithValidTokenSuccess() throws Exception {
        mockMvc.perform(get("/api/users/id/1")
                        .header("X-User-JWT", userJwtToken)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void getUserInfoByUserIdWithUserIdMismatch() throws Exception {
        mockMvc.perform(get("/api/users/id/2")
                        .header("X-User-JWT", userJwtToken)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserInfoByUsernameSuccess() throws Exception {
        mockMvc.perform(get("/api/users/username/user1")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void updatePasswordWithValidDataSuccess() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest("newPassword123", "newPassword123");

        mockMvc.perform(put("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-JWT", userJwtToken)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updatePasswordWithPasswordMismatch() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest("newPassword123", "differentPassword");

        mockMvc.perform(put("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-JWT", userJwtToken)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserInfoWithValidDataSuccess() throws Exception {
        UpdateUserInfoRequest request = new UpdateUserInfoRequest(
                "Updated Name",
                "updated@example.com",
                LocalDate.of(1990, 5, 15)
        );

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-JWT", userJwtToken)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void getAllUsersSuccess() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_accounts-access"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
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