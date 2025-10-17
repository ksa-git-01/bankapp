package ru.yandex.practicum.frontui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.frontui.dto.*;
import ru.yandex.practicum.frontui.exception.AccountsServiceException;
import ru.yandex.practicum.frontui.model.AccountView;
import ru.yandex.practicum.frontui.model.Currency;
import ru.yandex.practicum.frontui.service.AccountsClient;
import ru.yandex.practicum.frontui.service.CashClient;
import ru.yandex.practicum.frontui.service.TransferClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final AccountsClient accountsClient;
    private final CashClient cashClient;
    private final TransferClient transferClient;

    @GetMapping("/")
    public String mainPage(Authentication authentication, Model model) {
        String username = authentication.getName();

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");

        UserInfoResponse userInfo = accountsClient.getUserInfoByUserId(userId);

        List<AccountDto> userAccounts = accountsClient.getUserAccounts(userId);

        List<AccountView> accountViews = createAccountViews(userAccounts);

        List<UserListDto> allUsers = accountsClient.getAllUsers().stream()
                .filter(user -> !user.id().equals(userId))
                .collect(Collectors.toList());

        model.addAttribute("login", username);
        model.addAttribute("userId", userId);
        model.addAttribute("name", userInfo.name());
        model.addAttribute("email", userInfo.email());
        model.addAttribute("birthdate", userInfo.birthdate());
        model.addAttribute("accounts", accountViews);
        model.addAttribute("currency", Currency.values());
        model.addAttribute("users", allUsers);

        return "main";
    }

    private List<AccountView> createAccountViews(List<AccountDto> userAccounts) {
        List<AccountView> views = new ArrayList<>();

        for (Currency currency : Currency.values()) {
            AccountDto existingAccount = userAccounts.stream()
                    .filter(acc -> acc.currency().equals(currency.name()))
                    .findFirst()
                    .orElse(null);

            if (existingAccount != null) {
                views.add(AccountView.fromDto(existingAccount));
            } else {
                views.add(AccountView.empty(currency));
            }
        }

        return views;
    }

    @PostMapping("/user/{login}/editPassword")
    public String editPassword(
            @PathVariable String login,
            @RequestParam String password,
            @RequestParam("confirm_password") String confirmPassword,
            Authentication authentication,
            Model model) {

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");

        try {
            UpdatePasswordRequest request = new UpdatePasswordRequest(password, confirmPassword);
            accountsClient.updatePassword(userId, request);

            log.debug("Password changed successfully for user: {}", login);
            return "redirect:/";

        } catch (AccountsServiceException e) {
            log.error("Failed to change password: {}", e.getMessage());
            model.addAttribute("passwordErrors", List.of(e.getMessage()));
            return mainPage(authentication, model);
        }
    }

    @PostMapping("/user/{login}/editUserAccounts")
    public String editUserInfo(
            @PathVariable String login,
            @RequestParam String name,
            @RequestParam(required = false) LocalDate birthdate,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) List<String> account,
            Authentication authentication,
            Model model) {

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");

        try {
            UserInfoResponse currentInfo = accountsClient.getUserInfoByUserId(userId);
            if (birthdate == null) {
                birthdate = currentInfo.birthdate();
            }
            if (email == null) {
                email = currentInfo.email();
            }

            UpdateUserInfoRequest request = new UpdateUserInfoRequest(name, email, birthdate);
            accountsClient.updateUserInfo(userId, request);

            if (account != null) {
                List<AccountDto> existingAccounts = accountsClient.getUserAccounts(userId);

                for (String currency : account) {
                    boolean exists = existingAccounts.stream()
                            .anyMatch(acc -> acc.currency().equals(currency));

                    if (!exists) {
                        accountsClient.createAccount(userId, currency);
                        log.debug("Account created: user={}, currency={}", login, currency);
                    }
                }

                for (AccountDto existingAccount : existingAccounts) {
                    if (!account.contains(existingAccount.currency())) {
                        accountsClient.deleteAccount(userId, existingAccount.currency());
                        log.debug("Account deleted: user={}, currency={}", login, existingAccount.currency());
                    }
                }
            }

            log.debug("User info updated successfully for user: {}", login);
            return "redirect:/";

        } catch (AccountsServiceException e) {
            log.error("Failed to update user info: {}", e.getMessage());
            model.addAttribute("userAccountsErrors", List.of(e.getMessage()));
            return mainPage(authentication, model);
        }
    }

    @PostMapping("/user/{login}/cash")
    public String processCash(
            @PathVariable String login,
            @RequestParam String currency,
            @RequestParam Double value,
            @RequestParam String action,
            Authentication authentication,
            Model model) {

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");

        try {
            String operation = "PUT".equals(action) ? "DEPOSIT" : "GET".equals(action) ? "WITHDRAW" : "UNDEFINED";
            cashClient.processCashOperation(userId, operation, currency, value);

            log.debug("Cash operation successful: user={}, operation={}", login, operation);
            return "redirect:/";

        } catch (RuntimeException e) {
            log.error("Cash operation failed: {}", e.getMessage());
            model.addAttribute("cashErrors", List.of(e.getMessage()));
            return mainPage(authentication, model);
        }
    }

    @PostMapping("/user/{login}/transfer")
    public String processTransfer(
            @PathVariable String login,
            @RequestParam(name = "to_login", required = false) String toLogin,
            @RequestParam(name = "from_currency") String fromCurrency,
            @RequestParam(name = "to_currency") String toCurrency,
            @RequestParam Double value,
            Authentication authentication,
            Model model) {

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long fromUserId = (Long) details.get("userId");

        try {
            Long toUserId;

            // Если toLogin пустой или равен текущему - перевод себе
            if (toLogin == null || toLogin.isEmpty() || toLogin.equals(login)) {
                toUserId = fromUserId;
            } else {
                UserInfoResponse toUser = accountsClient.getUserInfoByUsername(toLogin);
                toUserId = toUser.id();
            }

            transferClient.transfer(fromUserId, toUserId, fromCurrency, toCurrency, value);

            log.debug("Transfer successful: from user {} to user {}", fromUserId, toUserId);
            return "redirect:/";

        } catch (RuntimeException e) {
            log.error("Transfer failed: {}", e.getMessage());
            model.addAttribute("transferErrors", List.of(e.getMessage()));
            return mainPage(authentication, model);
        }
    }
}