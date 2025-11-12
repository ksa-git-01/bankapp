package ru.yandex.practicum.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sendNotificationSuccess() throws Exception {
        String requestBody = """
            {
                "userId": 123,
                "type": "TRANSFER",
                "message": "Transfer completed",
                "amount": 100.50,
                "currency": "USD"
            }
            """;

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_notifications-access"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification sent successfully"));
    }

    @Test
    void sendNotificationWithoutToken() throws Exception {
        String requestBody = """
                {
                    "userId": 123,
                    "type": "TRANSFER",
                    "message": "Transfer completed"
                }
                """;

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sendNotificationWithTokenWithoutRequiredRole() throws Exception {
        String requestBody = """
            {
                "userId": 123,
                "type": "TRANSFER",
                "message": "Transfer completed"
            }
            """;

        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_other-role"))
                        ))
                .andExpect(status().isForbidden());
    }
}