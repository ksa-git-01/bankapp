package ru.yandex.practicum.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.transfer.dto.ExchangeRate;
import ru.yandex.practicum.transfer.dto.ExchangeRateResponse;
import ru.yandex.practicum.transfer.dto.TransferRequest;
import ru.yandex.practicum.transfer.dto.TransferResponse;
import ru.yandex.practicum.transfer.exception.TransferException;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountsClient accountsClient;
    private final NotificationsClient notificationsClient;
    private final ExchangeClient exchangeClient;

    private static String RUB = "RUB";

    public TransferResponse transfer(TransferRequest request) {
        log.debug("Processing transfer: from user {} to user {}, amount {} {}",
                request.fromUserId(), request.toUserId(),
                request.amount(), request.fromCurrency());

        BigDecimal convertedAmount = request.amount();

        // Конвертация валюты
        if (!request.fromCurrency().equals(request.toCurrency())) {
            try {
                ExchangeRateResponse response = exchangeClient.getRates();

                // Если одна из валют RUB - прямая конвертация
                if (request.fromCurrency().equals(RUB) || request.toCurrency().equals(RUB)) {
                    BigDecimal ratio = response.exchangeRates().stream()
                            .filter(exchangeRate -> exchangeRate.currencyFrom().equals(request.fromCurrency())
                                    && exchangeRate.currencyTo().equals(request.toCurrency()))
                            .map(ExchangeRate::ratio)
                            .findFirst()
                            .orElse(BigDecimal.ONE);

                    convertedAmount = request.amount().multiply(ratio);
                } else {
                    // Конвертация через RUB: fromCurrency -> RUB -> toCurrency
                    BigDecimal ratioToRub = response.exchangeRates().stream()
                            .filter(exchangeRate -> exchangeRate.currencyFrom().equals(request.fromCurrency())
                                    && exchangeRate.currencyTo().equals(RUB))
                            .map(ExchangeRate::ratio)
                            .findFirst()
                            .orElse(BigDecimal.ONE);

                    BigDecimal ratioFromRub = response.exchangeRates().stream()
                            .filter(exchangeRate -> exchangeRate.currencyFrom().equals(RUB)
                                    && exchangeRate.currencyTo().equals(request.toCurrency()))
                            .map(ExchangeRate::ratio)
                            .findFirst()
                            .orElse(BigDecimal.ONE);

                    convertedAmount = request.amount().multiply(ratioToRub).multiply(ratioFromRub);
                }
            } catch (Exception e) {
                log.error("Failed to get exchange rates", e);
                throw new TransferException("Transfer failed: " + e.getMessage());
            }
        }

        try {
            // Снять со счета отправителя
            BigDecimal fromBalance = accountsClient.withdraw(
                    request.fromUserId(),
                    request.fromCurrency(),
                    request.amount()
            );

            log.debug("Withdrawn from sender. New balance: {}", fromBalance);

            try {
                // Пополнить счет получателя
                BigDecimal toBalance = accountsClient.deposit(
                        request.toUserId(),
                        request.toCurrency(),
                        convertedAmount
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