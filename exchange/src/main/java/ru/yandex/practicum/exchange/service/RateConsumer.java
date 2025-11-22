package ru.yandex.practicum.exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.exchange.dto.ExchangeRate;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateConsumer {
    private final RateService rateService;

    @KafkaListener(
            topics = "${kafka.topics.exchange}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotification(
            @Payload ExchangeRate rate,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.debug("Received rate from partition {} offset {}: CurrencyFrom={}, CurrencyTo={}, Ratio={}",
                partition, offset, rate.getCurrencyFrom(), rate.getCurrencyTo(), rate.getRatio());

        try {
            rateService.createRate(rate);
            log.debug("Successfully rate");
        } catch (Exception e) {
            log.error("Failed to process rate", e);
            throw e;
        }
    }
}
