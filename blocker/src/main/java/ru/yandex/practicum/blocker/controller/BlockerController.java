package ru.yandex.practicum.blocker.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.blocker.dto.OperationCheckRequest;
import ru.yandex.practicum.blocker.dto.OperationCheckResponse;
import ru.yandex.practicum.blocker.service.BlockerService;

@RestController
@RequestMapping("/api/blocker")
@RequiredArgsConstructor
@Slf4j
public class BlockerController {

    private final BlockerService blockerService;

    @PostMapping("/check")
    public ResponseEntity<OperationCheckResponse> checkOperation(
            @RequestBody OperationCheckRequest operationCheckRequest,
            HttpServletRequest httpRequest) {

        log.info("Received operation check request from userId: {}", operationCheckRequest.getUserId());
        
        Long authenticatedUserId = (Long) httpRequest.getAttribute("userId");

        if (authenticatedUserId == null || !operationCheckRequest.getUserId().equals(authenticatedUserId)) {
            log.warn("Access denied: user {} tried to access userId {}", authenticatedUserId, operationCheckRequest.getUserId());
            return ResponseEntity.status(403).build();
        }

        OperationCheckResponse response = blockerService.checkOperation(operationCheckRequest);

        return ResponseEntity.ok(response);
    }
}