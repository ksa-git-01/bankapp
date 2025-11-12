package ru.yandex.practicum.transfer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.transfer.dto.TransferRequest;
import ru.yandex.practicum.transfer.dto.TransferResponse;
import ru.yandex.practicum.transfer.exception.TransferException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class TransferServiceIntegrationTest {

    @Autowired
    private TransferService transferService;

    @MockitoBean
    private AccountsClient accountsClient;

    @MockitoBean
    private NotificationsClient notificationsClient;

    @BeforeEach
    void setUp() {
        reset(accountsClient, notificationsClient);
    }

    @Test
    void transferSuccess() {
        TransferRequest request = new TransferRequest(1L, 2L, "RUB", "RUB", BigDecimal.valueOf(100.0));

        when(accountsClient.withdraw(1L, "RUB", BigDecimal.valueOf(100.0))).thenReturn(BigDecimal.valueOf(900.0));
        when(accountsClient.deposit(2L, "RUB", BigDecimal.valueOf(100.0))).thenReturn(BigDecimal.valueOf(1100.0));

        TransferResponse response = transferService.transfer(request);

        assertTrue(response.success());
        assertEquals("Transfer completed successfully", response.message());
        assertEquals(BigDecimal.valueOf(900.0), response.fromBalance());
        assertEquals(BigDecimal.valueOf(1100.0), response.toBalance());

        verify(accountsClient).withdraw(1L, "RUB", BigDecimal.valueOf(100.0));
        verify(accountsClient).deposit(2L, "RUB", BigDecimal.valueOf(100.0));
        verify(notificationsClient, times(2)).sendNotification(anyLong(), anyString(), anyString(), eq(BigDecimal.valueOf(100.0)), anyString());
    }

    @Test
    void transferWithdrawFailsThrowsTransferException() {
        TransferRequest request = new TransferRequest(1L, 2L, "RUB", "RUB", BigDecimal.valueOf(100.0));

        when(accountsClient.withdraw(1L, "RUB", BigDecimal.valueOf(100.0)))
                .thenThrow(new RuntimeException("Insufficient funds"));

        TransferException exception = assertThrows(TransferException.class, () -> {
            transferService.transfer(request);
        });

        assertTrue(exception.getMessage().contains("Transfer failed"));
        verify(accountsClient).withdraw(1L, "RUB", BigDecimal.valueOf(100.0));
        verify(accountsClient, never()).deposit(anyLong(), anyString(), BigDecimal.valueOf(anyDouble()));
        verify(notificationsClient, never()).sendNotification(anyLong(), anyString(), anyString(), BigDecimal.valueOf(anyDouble()), anyString());
    }

    @Test
    void transferDepositFailsRollbacksWithdrawal() {
        TransferRequest request = new TransferRequest(1L, 2L, "RUB", "RUB", BigDecimal.valueOf(100.0));

        when(accountsClient.withdraw(1L, "RUB", BigDecimal.valueOf(100.0))).thenReturn(BigDecimal.valueOf(900.0));
        when(accountsClient.deposit(2L, "RUB", BigDecimal.valueOf(100.0)))
                .thenThrow(new RuntimeException("Deposit failed"));
        when(accountsClient.deposit(1L, "RUB", BigDecimal.valueOf(100.0))).thenReturn(BigDecimal.valueOf(1000.0));

        TransferException exception = assertThrows(TransferException.class, () -> {
            transferService.transfer(request);
        });

        assertTrue(exception.getMessage().contains("Transfer failed"));
        verify(accountsClient).withdraw(1L, "RUB", BigDecimal.valueOf(100.0));
        verify(accountsClient).deposit(2L, "RUB", BigDecimal.valueOf(100.0));
        verify(accountsClient).deposit(1L, "RUB", BigDecimal.valueOf(100.0));
        verify(notificationsClient, never()).sendNotification(anyLong(), anyString(), anyString(), BigDecimal.valueOf(anyDouble()), anyString());
    }

    @Test
    void transferNotificationFailsTransferStillSucceeds() {
        TransferRequest request = new TransferRequest(1L, 2L, "RUB", "RUB", BigDecimal.valueOf(100.0));

        when(accountsClient.withdraw(1L, "RUB", BigDecimal.valueOf(100.0))).thenReturn(BigDecimal.valueOf(900.0));
        when(accountsClient.deposit(2L, "RUB", BigDecimal.valueOf(100.0))).thenReturn(BigDecimal.valueOf(1100.0));
        doThrow(new RuntimeException("Notification service unavailable"))
                .when(notificationsClient).sendNotification(anyLong(), anyString(), anyString(), BigDecimal.valueOf(anyDouble()), anyString());

        TransferResponse response = transferService.transfer(request);

        assertTrue(response.success());
        assertEquals("Transfer completed successfully", response.message());
        verify(accountsClient).withdraw(1L, "RUB", BigDecimal.valueOf(100.0));
        verify(accountsClient).deposit(2L, "RUB", BigDecimal.valueOf(100.0));
    }
}