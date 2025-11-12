package ru.yandex.practicum.frontui.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.frontui.dto.AuthResponse;
import ru.yandex.practicum.frontui.service.AccountsClient;
import ru.yandex.practicum.frontui.service.AuthService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountsClient accountsClient;

    @MockitoBean
    private AuthService authService;

    @Test
    void showRegistrationForm() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

    @Test
    void registerWithValidData() throws Exception {
        when(authService.authenticate(anyString(), anyString()))
                .thenReturn(new AuthResponse("token", "Bearer", 1L, "testuser", "USER"));

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("login", "testuser")
                        .param("password", "password123")
                        .param("confirm_password", "password123")
                        .param("name", "Test User")
                        .param("email", "test@yandex.ru")
                        .param("birthdate", "1990-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(cookie().exists("JWT-TOKEN"))
                .andExpect(cookie().httpOnly("JWT-TOKEN", true))
                .andExpect(cookie().path("JWT-TOKEN", "/"))
                .andExpect(cookie().maxAge("JWT-TOKEN", 3600));
    }

    @Test
    void registerWhenAutoLoginFails() throws Exception {
        when(authService.authenticate(anyString(), anyString()))
                .thenThrow(new RuntimeException("Authentication failed"));

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("login", "testuser")
                        .param("password", "password123")
                        .param("confirm_password", "password123")
                        .param("name", "Test User")
                        .param("email", "test@yandex.ru")
                        .param("birthdate", "1990-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}