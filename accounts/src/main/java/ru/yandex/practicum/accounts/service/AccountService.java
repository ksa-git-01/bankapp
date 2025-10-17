package ru.yandex.practicum.accounts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.accounts.dto.AccountDto;
import ru.yandex.practicum.accounts.dto.CreateAccountRequest;
import ru.yandex.practicum.accounts.exception.ValidationException;
import ru.yandex.practicum.accounts.model.Account;
import ru.yandex.practicum.accounts.repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    public List<AccountDto> getUserAccounts(Long userId) {
        log.debug("Getting accounts for user: {}", userId);

        return accountRepository.findByUserId(userId).stream()
                .map(account -> new AccountDto(
                        account.getId(),
                        account.getCurrency(),
                        account.getBalance()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountDto createAccount(CreateAccountRequest request) {
        log.debug("Creating account for user {} with currency {}",
                request.getUserId(), request.getCurrency());

        // Проверяем, нет ли уже счета в этой валюте
        if (accountRepository.findByUserIdAndCurrency(
                request.getUserId(), request.getCurrency()).isPresent()) {
            throw new ValidationException(List.of(
                    "Счет в валюте " + request.getCurrency() + " уже существует"
            ));
        }

        Account account = new Account();
        account.setUserId(request.getUserId());
        account.setCurrency(request.getCurrency());
        account.setBalance(0.0);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        Account saved = accountRepository.save(account);

        log.debug("Account created: id={}", saved.getId());

        return new AccountDto(saved.getId(), saved.getCurrency(), saved.getBalance());
    }

    @Transactional
    public Double deposit(Long userId, String currency, Double amount) {
        log.debug("Deposit: user={}, currency={}, amount={}", userId, currency, amount);

        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }

        Account account = accountRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Счет в валюте " + currency + " не найден"
                ));

        account.setBalance(account.getBalance() + amount);
        account.setUpdatedAt(LocalDateTime.now());

        Account updated = accountRepository.save(account);

        log.debug("Deposit completed. New balance: {}", updated.getBalance());

        return updated.getBalance();
    }

    @Transactional
    public Double withdraw(Long userId, String currency, Double amount) {
        log.debug("Withdraw: user={}, currency={}, amount={}", userId, currency, amount);

        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }

        Account account = accountRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Счет в валюте " + currency + " не найден"
                ));

        if (account.getBalance() < amount) {
            throw new IllegalArgumentException("Недостаточно средств на счете");
        }

        account.setBalance(account.getBalance() - amount);
        account.setUpdatedAt(LocalDateTime.now());

        Account updated = accountRepository.save(account);

        log.debug("Withdraw completed. New balance: {}", updated.getBalance());

        return updated.getBalance();
    }

    @Transactional
    public void deleteAccount(Long userId, String currency) {
        log.debug("Deleting account: user={}, currency={}", userId, currency);

        Account account = accountRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Счет в валюте " + currency + " не найден"
                ));

        if (account.getBalance() > 0) {
            throw new IllegalArgumentException(
                    "Невозможно удалить счет с ненулевым балансом"
            );
        }

        accountRepository.delete(account);

        log.debug("Account deleted");
    }
}