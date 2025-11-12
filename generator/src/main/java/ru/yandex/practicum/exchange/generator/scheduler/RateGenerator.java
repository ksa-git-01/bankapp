package ru.yandex.practicum.exchange.generator.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.exchange.generator.service.RateService;

@Service
@RequiredArgsConstructor
public class RateGenerator {
    private final RateService rateService;

    @Scheduled(fixedRate = 5000)
    public void generateRates(){
        rateService.generateRates();
    }
}
