package ru.yandex.practicum.frontui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.frontui.dto.RegistrationRequest;
import ru.yandex.practicum.frontui.dto.RegistrationResponse;
import ru.yandex.practicum.frontui.exception.RegistrationException;
import ru.yandex.practicum.frontui.service.AccountsClient;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final AccountsClient accountsClient;

    @GetMapping("/signup")
    public String showRegistrationForm(Model model) {
        return "signup";
    }

    @PostMapping("/signup")
    public String register(
            @RequestParam String login,
            @RequestParam String password,
            @RequestParam("confirm_password") String confirmPassword,
            @RequestParam String name,
            @RequestParam(required = false) String email,
            @RequestParam LocalDate birthdate,
            Model model) {

        log.info("Registration form submitted for user: {}", login);

        RegistrationRequest request = new RegistrationRequest(
                login,
                password,
                confirmPassword,
                name,
                email,
                birthdate
        );

        try {
            RegistrationResponse response = accountsClient.registerUser(request);

            log.info("Registration successful: {}", response.getUsername());

            // Редирект на логин с сообщением
            return "redirect:/login?registered";

        } catch (RegistrationException e) {
            log.error("Registration failed", e);

            // Возвращаем форму с ошибкой
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("login", login);
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("birthdate", birthdate);

            return "signup";
        }
    }
}