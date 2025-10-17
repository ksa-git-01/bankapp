package ru.yandex.practicum.blocker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.blocker.dto.OperationCheckRequest;
import ru.yandex.practicum.blocker.dto.OperationCheckResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BlockerServiceIntegrationTest {

    @Autowired
    private BlockerService blockerService;

    @Test
    void checkOperationWithNormalAmount() {
        OperationCheckRequest request = new OperationCheckRequest(1L, "WITHDRAW", "RUB", 5000.0);

        OperationCheckResponse response = blockerService.checkOperation(request);

        assertFalse(response.blocked());
        assertNull(response.reason());
    }

    @Test
    void checkOperationWithAmountAboveLimit() {
        OperationCheckRequest request = new OperationCheckRequest(1L, "WITHDRAW", "RUB", 100001.0);

        OperationCheckResponse response = blockerService.checkOperation(request);

        assertTrue(response.blocked());
        assertNotNull(response.reason());
        assertTrue(response.reason().contains("превышает лимит 100000"));
    }

    @Test
    void checkOperationDepositOperationSuccess() {
        OperationCheckRequest request = new OperationCheckRequest(1L, "DEPOSIT", "RUB", 50000.0);

        OperationCheckResponse response = blockerService.checkOperation(request);

        assertFalse(response.blocked());
        assertNull(response.reason());
    }

    @Test
    void checkOperationWithdrawOperationSuccess() {
        OperationCheckRequest request = new OperationCheckRequest(1L, "WITHDRAW", "RUB", 50000.0);

        OperationCheckResponse response = blockerService.checkOperation(request);

        assertFalse(response.blocked());
        assertNull(response.reason());
    }
}