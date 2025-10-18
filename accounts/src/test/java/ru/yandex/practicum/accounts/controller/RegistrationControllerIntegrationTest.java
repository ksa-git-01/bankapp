package ru.yandex.practicum.accounts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.accounts.configuration.TestContainersConfiguration;
import ru.yandex.practicum.accounts.dto.RegistrationRequest;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class RegistrationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerWithValidDataSuccess() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "newuser",
                "password123",
                "password123",
                "New User",
                "newuser@yandex.com",
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void registerWithExistingUsername() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "user1",
                "password123",
                "password123",
                "Test User",
                "test@yandex.com",
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void registerWithPasswordMismatch() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "testuser",
                "password123",
                "password456",
                "Test User",
                "test@yandex.com",
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void registerWithUnderageUser() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "younguser",
                "password123",
                "password123",
                "Young User",
                "young@yandex.com",
                LocalDate.now().minusYears(15)
        );

        mockMvc.perform(post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void registerWithInvalidEmail() throws Exception {
        RegistrationRequest request = new RegistrationRequest(
                "testuser",
                "password123",
                "password123",
                "Test User",
                "invalid-email",
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }
}