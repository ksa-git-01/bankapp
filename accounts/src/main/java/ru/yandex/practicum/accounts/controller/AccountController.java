package ru.yandex.practicum.accounts.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.accounts.dto.*;
import ru.yandex.practicum.accounts.exception.ValidationException;
import ru.yandex.practicum.accounts.service.AccountService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountDto>> getUserAccounts(
            @PathVariable Long userId,
            HttpServletRequest request) {

        Long authenticatedUserId = (Long) request.getAttribute("userId");

        if (authenticatedUserId == null || !userId.equals(authenticatedUserId)) {
            return ResponseEntity.status(403).build();
        }

        List<AccountDto> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(
            @RequestBody CreateAccountRequest request,
            HttpServletRequest httpRequest) {

        Long authenticatedUserId = (Long) httpRequest.getAttribute("userId");

        if (authenticatedUserId == null || !request.getUserId().equals(authenticatedUserId)) {
            return ResponseEntity.status(403).build();
        }

        AccountDto account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountOperationResponse> deposit(
            @RequestBody DepositRequest request) {

        log.info("Deposit request: user={}, currency={}, amount={}",
                request.getUserId(), request.getCurrency(), request.getAmount());

        try {
            BigDecimal newBalance = accountService.deposit(
                    request.getUserId(),
                    request.getCurrency(),
                    request.getAmount()
            );

            return ResponseEntity.ok(new AccountOperationResponse(
                    true,
                    "Счет пополнен",
                    newBalance
            ));

        } catch (IllegalArgumentException e) {
            log.error("Deposit failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AccountOperationResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<AccountOperationResponse> withdraw(
            @RequestBody WithdrawRequest request) {

        log.info("Withdraw request: user={}, currency={}, amount={}",
                request.getUserId(), request.getCurrency(), request.getAmount());

        try {
            BigDecimal newBalance = accountService.withdraw(
                    request.getUserId(),
                    request.getCurrency(),
                    request.getAmount()
            );

            return ResponseEntity.ok(new AccountOperationResponse(
                    true,
                    "Средства сняты со счета",
                    newBalance
            ));

        } catch (IllegalArgumentException e) {
            log.error("Withdraw failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AccountOperationResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        }
    }

    @DeleteMapping("/user/{userId}/currency/{currency}")
    public ResponseEntity<Map<String, String>> deleteAccount(
            @PathVariable Long userId,
            @PathVariable String currency,
            HttpServletRequest request) {

        Long authenticatedUserId = (Long) request.getAttribute("userId");

        if (authenticatedUserId == null || !userId.equals(authenticatedUserId)) {
            return ResponseEntity.status(403).build();
        }

        try {
            accountService.deleteAccount(userId, currency);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Счет удален");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errors", e.getErrors());
        errorResponse.put("message", "Ошибка валидации");
        return ResponseEntity.badRequest().body(errorResponse);
    }
}