package ru.yandex.practicum.exchange.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.exchange.dto.RatesRequest;
import ru.yandex.practicum.exchange.dto.RatesResponse;
import ru.yandex.practicum.exchange.service.RateService;

import java.util.List;

@RestController
@RequestMapping("/api/rates")
@RequiredArgsConstructor
@Slf4j
public class RatesController {
    private final RateService rateService;

    @GetMapping
    public ResponseEntity<List<RatesResponse>> getRates(){
        try {
            List<RatesResponse> response = rateService.getRates();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
