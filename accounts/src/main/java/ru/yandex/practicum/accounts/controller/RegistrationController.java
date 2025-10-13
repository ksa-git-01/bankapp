package ru.yandex.practicum.accounts.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.accounts.dto.RegistrationRequest;
import ru.yandex.practicum.accounts.dto.RegistrationResponse;
import ru.yandex.practicum.accounts.exception.ValidationException;
import ru.yandex.practicum.accounts.service.RegistrationService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/api/registration")
    public ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationRequest request) {
        log.info("Registration request received for user: {}", request.getLogin());

        try {
            RegistrationResponse response = registrationService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ValidationException e) {
            log.warn("Validation failed: {}", e.getErrors());
            throw e;
        }
    }
}