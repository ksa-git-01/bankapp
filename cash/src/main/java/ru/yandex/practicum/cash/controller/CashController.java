package ru.yandex.practicum.cash.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.cash.dto.CashOperationRequest;
import ru.yandex.practicum.cash.dto.CashOperationResponse;
import ru.yandex.practicum.cash.exception.OperationBlockedException;
import ru.yandex.practicum.cash.service.CashService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
@Slf4j
public class CashController {

    private final CashService cashService;

    @PostMapping("/operation")
    public ResponseEntity<CashOperationResponse> processCashOperation(
            @RequestBody CashOperationRequest request,
            HttpServletRequest httpRequest) {

        Long authenticatedUserId = (Long) httpRequest.getAttribute("userId");

        if (authenticatedUserId == null || !request.getUserId().equals(authenticatedUserId)) {
            log.warn("Access denied: user {} tried to access userId {}", authenticatedUserId, request.getUserId());
            return ResponseEntity.status(403).body(
                    new CashOperationResponse(false, "Access denied", null)
            );
        }

        log.debug("Cash operation request: userId={}, operation={}, amount={}, currency={}",
                request.getUserId(), request.getOperation(), request.getAmount(), request.getCurrency());

        CashOperationResponse response = cashService.processCashOperation(request);

        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(OperationBlockedException.class)
    public ResponseEntity<Map<String, Object>> handleOperationBlocked(OperationBlockedException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(403).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
}