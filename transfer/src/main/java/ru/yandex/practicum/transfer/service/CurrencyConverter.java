package ru.yandex.practicum.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.transfer.dto.ExchangeRateResponse;
import ru.yandex.practicum.transfer.exception.TransferException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyConverter {
    private final ExchangeClient exchangeClient;

    private static final String RUB = "RUB";

    public BigDecimal convertAmount(String fromCurrency, String toCurrency, BigDecimal amount) {
        try {
            List<ExchangeRateResponse> response = exchangeClient.getRates();

            // Если одна из валют RUB - прямая конвертация
            if (fromCurrency.equals(RUB) || toCurrency.equals(RUB)) {
                return directConversion(fromCurrency, toCurrency, amount, response);
            } else {
                // Конвертация через RUB: fromCurrency -> RUB -> toCurrency
                return viaRubConversion(fromCurrency, toCurrency, amount, response);
            }
        } catch (Exception e) {
            log.error("Failed to get exchange rates", e);
            throw new TransferException("Transfer failed: " + e.getMessage());
        }
    }

    private BigDecimal directConversion(String fromCurrency, String toCurrency, BigDecimal amount, List<ExchangeRateResponse> rateList) {
        BigDecimal ratio = rateList.stream()
                .filter(exchangeRate -> exchangeRate.currencyFrom().equals(fromCurrency)
                        && exchangeRate.currencyTo().equals(toCurrency))
                .map(ExchangeRateResponse::ratio)
                .findFirst()
                .orElse(BigDecimal.ONE);

        return amount.multiply(ratio);
    }

    private BigDecimal viaRubConversion(String fromCurrency, String toCurrency, BigDecimal amount, List<ExchangeRateResponse> rateList) {
        BigDecimal ratioToRub = rateList.stream()
                .filter(exchangeRate -> exchangeRate.currencyFrom().equals(fromCurrency)
                        && exchangeRate.currencyTo().equals(RUB))
                .map(ExchangeRateResponse::ratio)
                .findFirst()
                .orElse(BigDecimal.ONE);

        BigDecimal ratioFromRub = rateList.stream()
                .filter(exchangeRate -> exchangeRate.currencyFrom().equals(RUB)
                        && exchangeRate.currencyTo().equals(toCurrency))
                .map(ExchangeRateResponse::ratio)
                .findFirst()
                .orElse(BigDecimal.ONE);

        return amount.multiply(ratioToRub).multiply(ratioFromRub);
    }
}
