package ru.yandex.practicum.cash.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.cash.dto.CashOperationRequest;
import ru.yandex.practicum.cash.dto.CashOperationResponse;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class CashServiceIntegrationTest {

    @Autowired
    private CashService cashService;

    @MockitoBean
    private BlockerClient blockerClient;

    @MockitoBean
    private AccountsClient accountsClient;

    @MockitoBean
    private NotificationsClient notificationsClient;

    @BeforeEach
    void setUp() {
        reset(blockerClient, accountsClient, notificationsClient);
    }

    @Test
    void cashOperationDepositSuccess() {
        CashOperationRequest request = new CashOperationRequest(1L, "DEPOSIT", "RUB", BigDecimal.valueOf(1000.0));

        when(blockerClient.checkOperation(any(CashOperationRequest.class))).thenReturn(false);
        when(accountsClient.deposit(1L, "RUB", BigDecimal.valueOf(1000.0))).thenReturn(BigDecimal.valueOf(5000.0));

        CashOperationResponse response = cashService.processCashOperation(request);

        assertTrue(response.success());
        assertEquals("Operation completed successfully", response.message());
        assertEquals(BigDecimal.valueOf(5000.0), response.newBalance());

        verify(blockerClient).checkOperation(any(CashOperationRequest.class));
        verify(accountsClient).deposit(1L, "RUB", BigDecimal.valueOf(1000.0));
        verify(notificationsClient).sendNotification(eq(1L), eq("DEPOSIT"), anyString(), eq(BigDecimal.valueOf(1000.0)), eq("RUB"));
    }

    @Test
    void cashOperationWithdrawSuccess() {
        CashOperationRequest request = new CashOperationRequest(1L, "WITHDRAW", "RUB", BigDecimal.valueOf(500));

        when(blockerClient.checkOperation(any(CashOperationRequest.class))).thenReturn(false);
        when(accountsClient.withdraw(1L, "RUB", BigDecimal.valueOf(500))).thenReturn(BigDecimal.valueOf(3500.0));

        CashOperationResponse response = cashService.processCashOperation(request);

        assertTrue(response.success());
        assertEquals("Operation completed successfully", response.message());
        assertEquals(BigDecimal.valueOf(3500.0), response.newBalance());

        verify(blockerClient).checkOperation(any(CashOperationRequest.class));
        verify(accountsClient).withdraw(1L, "RUB", BigDecimal.valueOf(500));
        verify(notificationsClient).sendNotification(eq(1L), eq("WITHDRAW"), anyString(), eq(BigDecimal.valueOf(500)), eq("RUB"));
    }

    @Test
    void cashOperationWhenBlocked() {
        CashOperationRequest request = new CashOperationRequest(1L, "WITHDRAW", "RUB", BigDecimal.valueOf(150000.0));

        when(blockerClient.checkOperation(any(CashOperationRequest.class))).thenReturn(true);

        CashOperationResponse response = cashService.processCashOperation(request);

        assertFalse(response.success());
        assertEquals("Operation blocked due to suspicious activity", response.message());
        assertNull(response.newBalance());

        verify(blockerClient).checkOperation(any(CashOperationRequest.class));
        verify(accountsClient, never()).deposit(anyLong(), anyString(), BigDecimal.valueOf(anyDouble()));
        verify(accountsClient, never()).withdraw(anyLong(), anyString(), BigDecimal.valueOf(anyDouble()));
        verify(notificationsClient, never()).sendNotification(anyLong(), anyString(), anyString(), BigDecimal.valueOf(anyDouble()), anyString());
    }

    @Test
    void cashOperationWhenAccountsClientFails() {
        CashOperationRequest request = new CashOperationRequest(1L, "DEPOSIT", "RUB", BigDecimal.valueOf(1000.0));

        when(blockerClient.checkOperation(any(CashOperationRequest.class))).thenReturn(false);
        when(accountsClient.deposit(1L, "RUB", BigDecimal.valueOf(1000.0)))
                .thenThrow(new RuntimeException("Insufficient funds"));

        CashOperationResponse response = cashService.processCashOperation(request);

        assertFalse(response.success());
        assertTrue(response.message().contains("Operation failed"));
        assertNull(response.newBalance());

        verify(blockerClient).checkOperation(any(CashOperationRequest.class));
        verify(accountsClient).deposit(1L, "RUB", BigDecimal.valueOf(1000.0));
        verify(notificationsClient, never()).sendNotification(anyLong(), anyString(), anyString(), BigDecimal.valueOf(anyDouble()), anyString());
    }

    @Test
    void cashOperationWhenNotificationFails() {
        CashOperationRequest request = new CashOperationRequest(1L, "DEPOSIT", "RUB", BigDecimal.valueOf(1000.0));

        when(blockerClient.checkOperation(any(CashOperationRequest.class))).thenReturn(false);
        when(accountsClient.deposit(1L, "RUB", BigDecimal.valueOf(1000.0))).thenReturn(BigDecimal.valueOf(5000.0));
        doThrow(new RuntimeException("Notification service unavailable"))
                .when(notificationsClient).sendNotification(anyLong(), anyString(), anyString(), BigDecimal.valueOf(anyDouble()), anyString());

        CashOperationResponse response = cashService.processCashOperation(request);

        assertTrue(response.success());
        assertEquals("Operation completed successfully", response.message());
        assertEquals(BigDecimal.valueOf(5000.0), response.newBalance());

        verify(blockerClient).checkOperation(any(CashOperationRequest.class));
        verify(accountsClient).deposit(1L, "RUB", BigDecimal.valueOf(1000.0));
    }

    @Test
    void cashOperationWithInvalidOperationType() {
        CashOperationRequest request = new CashOperationRequest(1L, "INVALID", "RUB", BigDecimal.valueOf(1000.0));

        when(blockerClient.checkOperation(any(CashOperationRequest.class))).thenReturn(false);

        CashOperationResponse response = cashService.processCashOperation(request);

        assertFalse(response.success());
        assertEquals("Invalid operation type", response.message());
        assertNull(response.newBalance());

        verify(blockerClient).checkOperation(any(CashOperationRequest.class));
        verify(accountsClient, never()).deposit(anyLong(), anyString(), BigDecimal.valueOf(anyDouble()));
        verify(accountsClient, never()).withdraw(anyLong(), anyString(), BigDecimal.valueOf(anyDouble()));
    }

    @Test
    void cashOperationWhenBlockerServiceFails() {
        CashOperationRequest request = new CashOperationRequest(1L, "DEPOSIT", "RUB", BigDecimal.valueOf(1000.0));

        when(blockerClient.checkOperation(any(CashOperationRequest.class))).thenReturn(false);
        when(accountsClient.deposit(1L, "RUB", BigDecimal.valueOf(1000.0))).thenReturn(BigDecimal.valueOf(5000.0));

        CashOperationResponse response = cashService.processCashOperation(request);

        assertTrue(response.success());
        verify(accountsClient).deposit(1L, "RUB", BigDecimal.valueOf(1000.0));
    }
}