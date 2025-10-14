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
import ru.yandex.practicum.frontui.dto.UpdatePasswordRequest;
import ru.yandex.practicum.frontui.dto.UpdateUserInfoRequest;
import ru.yandex.practicum.frontui.dto.UserInfoResponse;
import ru.yandex.practicum.frontui.exception.AccountsServiceException;
import ru.yandex.practicum.frontui.service.AccountsClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final AccountsClient accountsClient;

    @GetMapping("/")
    public String mainPage(Authentication authentication, Model model) {
        String username = authentication.getName();

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");

        UserInfoResponse userInfo = accountsClient.getUserInfo(userId);

        model.addAttribute("login", username);
        model.addAttribute("name", userInfo.getName());
        model.addAttribute("email", userInfo.getEmail());
        model.addAttribute("birthdate", userInfo.getBirthdate());

        return "main";
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

            log.info("Password changed successfully for user: {}", login);
            return "redirect:/?passwordSuccess";

        } catch (AccountsServiceException e) {
            log.error("Failed to change password: {}", e.getMessage());
            // Теперь сообщение уже обработано в AccountsClient
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
            Authentication authentication,
            Model model) {

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");

        try {
            if (birthdate == null) {
                UserInfoResponse currentInfo = accountsClient.getUserInfo(userId);
                birthdate = currentInfo.getBirthdate();
            }

            UpdateUserInfoRequest request = new UpdateUserInfoRequest(name, email, birthdate);
            accountsClient.updateUserInfo(userId, request);

            log.info("User info updated successfully for user: {}", login);
            return "redirect:/?userInfoSuccess";

        } catch (AccountsServiceException e) {
            log.error("Failed to update user info: {}", e.getMessage());
            // Теперь сообщение уже обработано в AccountsClient
            model.addAttribute("userAccountsErrors", List.of(e.getMessage()));
            return mainPage(authentication, model);
        }
    }
}