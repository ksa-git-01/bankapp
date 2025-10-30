package ru.yandex.practicum.exchange.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.exchange.dto.ExchangeHistoryRequest;
import ru.yandex.practicum.exchange.service.ExchangeService;

@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
@Slf4j
public class ExchangeController {
    private final ExchangeService exchangeService;

    @PostMapping
    public ResponseEntity<?> createHistory(@RequestBody ExchangeHistoryRequest request) {
        try {
            exchangeService.saveHistory(request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to save exchange history", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
