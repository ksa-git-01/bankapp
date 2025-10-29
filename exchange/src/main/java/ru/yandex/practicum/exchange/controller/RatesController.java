package ru.yandex.practicum.exchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.exchange.dto.RatesRequest;
import ru.yandex.practicum.exchange.dto.RatesResponse;
import ru.yandex.practicum.exchange.service.RateService;

import java.util.List;

@RestController
@RequestMapping("/api/rates")
@RequiredArgsConstructor
public class RatesController {
    private final RateService rateService;

    @PostMapping
    public ResponseEntity<Void> createRate(@RequestBody RatesRequest request){
        try {
            rateService.createRate(request.currencyFrom(), request.currencyTo(), request.ratio());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

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
