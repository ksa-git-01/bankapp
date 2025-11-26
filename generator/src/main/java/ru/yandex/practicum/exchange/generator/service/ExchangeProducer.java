package ru.yandex.practicum.exchange.generator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.exchange.generator.dto.ExchangeRate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeProducer {

    private final KafkaTemplate<String, ExchangeRate> kafkaTemplate;

    @Value("${kafka.topics.exchange}")
    private String exchangeTopic;

    public void sendRate(String currencyFrom, String currencyTo, BigDecimal ratio) {
        ExchangeRate rate = ExchangeRate.builder()
                .currencyFrom(currencyFrom)
                .currencyTo(currencyTo)
                .ratio(ratio)
                .build();

        try {
            CompletableFuture<SendResult<String, ExchangeRate>> future =
                    kafkaTemplate.send(exchangeTopic, rate);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Sent rate from {} to {} ratio {} to topic: {} partition: {}",
                            rate.getCurrencyFrom(),
                            rate.getCurrencyTo(),
                            rate.getRatio(),
                            exchangeTopic,
                            result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to send rate from {} to {} ratio {}",
                            rate.getCurrencyFrom(),
                            rate.getCurrencyTo(),
                            rate.getRatio(),
                            ex);
                }
            });
        } catch (Exception e) {
            log.error("Error sending rate to Kafka", e);
        }
    }
}
