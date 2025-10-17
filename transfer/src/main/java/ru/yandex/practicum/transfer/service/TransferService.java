package ru.yandex.practicum.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.transfer.dto.TransferRequest;
import ru.yandex.practicum.transfer.dto.TransferResponse;
import ru.yandex.practicum.transfer.exception.TransferException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountsClient accountsClient;
    private final NotificationsClient notificationsClient;

    public TransferResponse transfer(TransferRequest request) {
        log.debug("Processing transfer: from user {} to user {}, amount {} {}",
                request.fromUserId(), request.toUserId(),
                request.amount(), request.fromCurrency());

        try {
            // Снять со счета отправителя
            Double fromBalance = accountsClient.withdraw(
                    request.fromUserId(),
                    request.fromCurrency(),
                    request.amount()
            );

            log.debug("Withdrawn from sender. New balance: {}", fromBalance);

            try {
                // Пополнить счет получателя
                Double toBalance = accountsClient.deposit(
                        request.toUserId(),
                        request.toCurrency(),
                        request.amount()
                );

                log.debug("Deposited to receiver. New balance: {}", toBalance);

                // Отправить уведомления
                sendNotifications(request);

                return new TransferResponse(
                        true,
                        "Transfer completed successfully",
                        fromBalance,
                        toBalance
                );

            } catch (Exception e) {
                // Откатить списание если пополнение не удалось
                log.error("Failed to deposit to receiver, rolling back", e);
                accountsClient.deposit(
                        request.fromUserId(),
                        request.fromCurrency(),
                        request.amount()
                );
                throw new TransferException("Transfer failed: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("Transfer failed", e);
            throw new TransferException("Transfer failed: " + e.getMessage());
        }
    }

    private void sendNotifications(TransferRequest request) {
        try {
            // Уведомление отправителю
            notificationsClient.sendNotification(
                    request.fromUserId(),
                    "TRANSFER_SENT",
                    String.format("You sent %.2f %s to user %d",
                            request.amount(), request.toCurrency(), request.toUserId()),
                    request.amount(),
                    request.fromCurrency()
            );

            // Уведомление получателю
            notificationsClient.sendNotification(
                    request.toUserId(),
                    "TRANSFER_RECEIVED",
                    String.format("You received %.2f %s from user %d",
                            request.amount(), request.toCurrency(), request.fromUserId()),
                    request.amount(),
                    request.toCurrency()
            );

        } catch (Exception e) {
            // Продолжаем если отправка не удалась
            log.warn("Failed to send notifications: {}", e.getMessage());
        }
    }
}