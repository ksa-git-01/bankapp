package ru.yandex.practicum.blocker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.blocker.dto.OperationCheckRequest;
import ru.yandex.practicum.blocker.dto.OperationCheckResponse;

import java.math.BigDecimal;

@Service
@Slf4j
public class BlockerService {

    public OperationCheckResponse checkOperation(OperationCheckRequest request) {
        log.debug("Checking operation: user={}, operation={}, amount={}, currency={}",
                request.getUserId(), request.getOperation(), request.getAmount(), request.getCurrency());

        // Блокируем операции больше 100000
        if (request.getAmount().compareTo(BigDecimal.valueOf(100000)) > 0) {
            log.warn("Suspicious operation blocked: very large amount");
            return new OperationCheckResponse(
                    true,
                    "Операция заблокирована: сумма превышает лимит 100000"
            );
        }

        log.debug("Operation approved");
        return new OperationCheckResponse(false, null);
    }
}