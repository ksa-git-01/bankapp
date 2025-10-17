package ru.yandex.practicum.transfer.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.transfer.dto.TransferRequest;
import ru.yandex.practicum.transfer.dto.TransferResponse;
import ru.yandex.practicum.transfer.exception.TransferException;
import ru.yandex.practicum.transfer.service.TransferService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @RequestBody TransferRequest request,
            HttpServletRequest httpRequest) {

        Long authenticatedUserId = (Long) httpRequest.getAttribute("userId");

        if (authenticatedUserId == null || !request.getFromUserId().equals(authenticatedUserId)) {
            log.warn("Access denied: user {} tried to transfer from userId {}",
                    authenticatedUserId, request.getFromUserId());
            return ResponseEntity.status(403).body(
                    new TransferResponse(false, "Access denied", null, null)
            );
        }

        log.debug("Transfer request: from={}, to={}, amount={} {}",
                request.getFromUserId(), request.getToUserId(),
                request.getAmount(), request.getFromCurrency());

        TransferResponse response = transferService.transfer(request);

        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(TransferException.class)
    public ResponseEntity<Map<String, Object>> handleTransferException(TransferException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
}