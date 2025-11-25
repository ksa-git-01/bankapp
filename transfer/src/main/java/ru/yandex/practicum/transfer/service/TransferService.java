package ru.yandex.practicum.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.transfer.dto.*;
import ru.yandex.practicum.transfer.exception.TransferException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountsClient accountsClient;
    private final NotificationProducer notificationProducer;
    private final ExchangeClient exchangeClient;
    private final CurrencyConverter currencyConverter;

    public TransferResponse transfer(TransferRequest request) {
        log.info("Processing transfer: from user {} to user {}, amount {} {}",
                request.fromUserId(), request.toUserId(),
                request.amount(), request.fromCurrency());
        TransferResponse transferResponse;
        BigDecimal convertedAmount = request.amount();

        // Конвертация валюты
        if (!request.fromCurrency().equals(request.toCurrency())) {
            convertedAmount = currencyConverter.convertAmount(
                    request.fromCurrency(),
                    request.toCurrency(),
                    request.amount()
                    );
        }

        try {
            // Снять со счета отправителя
            BigDecimal fromBalance = accountsClient.withdraw(
                    request.fromUserId(),
                    request.fromCurrency(),
                    request.amount()
            );

            log.info("Withdrawn from sender. New balance: {}", fromBalance);

            try {
                // Пополнить счет получателя
                BigDecimal toBalance = accountsClient.deposit(
                        request.toUserId(),
                        request.toCurrency(),
                        convertedAmount
                );

                log.info("Deposited to receiver. New balance: {}", toBalance);

                // Отправить уведомления
                sendNotifications(request);

                transferResponse = new TransferResponse(
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

        saveConversionHistory(request, convertedAmount);

        return transferResponse;
    }

    private void saveConversionHistory(TransferRequest transferRequest, BigDecimal convertedAmount) {
        try {
            exchangeClient.createConversionHistory(
                    new ExchangeHistoryRequest(
                            transferRequest.fromUserId(),
                            transferRequest.fromCurrency(),
                            transferRequest.amount(),
                            transferRequest.toUserId(),
                            transferRequest.toCurrency(),
                            convertedAmount
                    ));
        }
        catch (Exception e) {
            log.warn("Saving transfer history failed", e);
        }
    }

    private void sendNotifications(TransferRequest request) {
        try {
            // Уведомление отправителю
            notificationProducer.sendNotification(
                    request.fromUserId(),
                    "TRANSFER_SENT",
                    String.format("You sent %.2f %s to user %d",
                            request.amount(), request.toCurrency(), request.toUserId())
            );

            // Уведомление получателю
            notificationProducer.sendNotification(
                    request.toUserId(),
                    "TRANSFER_RECEIVED",
                    String.format("You received %.2f %s from user %d",
                            request.amount(), request.toCurrency(), request.fromUserId())
            );

        } catch (Exception e) {
            // Продолжаем если отправка не удалась
            log.warn("Failed to send notifications: {}", e.getMessage());
        }
    }
}