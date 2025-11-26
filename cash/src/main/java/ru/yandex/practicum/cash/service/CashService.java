package ru.yandex.practicum.cash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.cash.dto.CashOperationRequest;
import ru.yandex.practicum.cash.dto.CashOperationResponse;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashService {

    private final BlockerClient blockerClient;
    private final AccountsClient accountsClient;
    private final NotificationProducer notificationProducer;

    public CashOperationResponse processCashOperation(CashOperationRequest request) {
        log.info("Processing cash operation: user={}, operation={}, amount={} {}",
                request.getUserId(), request.getOperation(),
                request.getAmount(), request.getCurrency());

        try {
            // 1. Проверка в Blocker
            boolean operationBlocked = blockerClient.checkOperation(request);

            if (operationBlocked) {
                log.warn("Suspicious operation detected for user {}", request.getUserId());
                return new CashOperationResponse(
                        false,
                        "Operation blocked due to suspicious activity",
                        null
                );
            }

            // 2. Выполнить операцию в Accounts
            BigDecimal newBalance;
            String notificationType;
            String notificationMessage;

            if ("DEPOSIT".equals(request.getOperation())) {
                newBalance = accountsClient.deposit(
                        request.getUserId(),
                        request.getCurrency(),
                        request.getAmount()
                );
                notificationType = "DEPOSIT";
                notificationMessage = String.format(
                        "Your account has been credited with %.2f %s",
                        request.getAmount(), request.getCurrency()
                );
                log.info("Deposit successful. New balance: {}", newBalance);

            } else if ("WITHDRAW".equals(request.getOperation())) {
                newBalance = accountsClient.withdraw(
                        request.getUserId(),
                        request.getCurrency(),
                        request.getAmount()
                );
                notificationType = "WITHDRAW";
                notificationMessage = String.format(
                        "%.2f %s has been withdrawn from your account",
                        request.getAmount(), request.getCurrency()
                );
                log.info("Withdraw successful. New balance: {}", newBalance);

            } else {
                return new CashOperationResponse(
                        false,
                        "Invalid operation type",
                        null
                );
            }
            try {
                // 3. Отправить уведомление
                notificationProducer.sendNotification(
                        request.getUserId(),
                        notificationType,
                        notificationMessage
                );
            } catch (Exception e) {
                // Если отправка уведомления упала, то все равно продолжаем
                log.warn("Notification failed for: user={}, operation={}, amount={} {}",
                        request.getUserId(), request.getOperation(),
                        request.getAmount(), request.getCurrency());
            }

            return new CashOperationResponse(
                    true,
                    "Operation completed successfully",
                    newBalance
            );

        } catch (Exception e) {
            log.error("Cash operation failed", e);
            return new CashOperationResponse(
                    false,
                    "Operation failed: " + e.getMessage(),
                    null
            );
        }
    }
}