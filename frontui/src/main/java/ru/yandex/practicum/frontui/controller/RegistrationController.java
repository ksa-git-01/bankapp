package ru.yandex.practicum.frontui.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.frontui.dto.AuthResponse;
import ru.yandex.practicum.frontui.dto.RegistrationRequest;
import ru.yandex.practicum.frontui.exception.RegistrationException;
import ru.yandex.practicum.frontui.service.AccountsClient;
import ru.yandex.practicum.frontui.service.AuthService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final AccountsClient accountsClient;
    private final AuthService authService;

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
            HttpServletResponse response,
            Model model) {
        try {
            RegistrationRequest request = new RegistrationRequest(
                    login,
                    password,
                    confirmPassword,
                    name,
                    email,
                    birthdate
            );

            accountsClient.registerUser(request);

            try {
                AuthResponse authResponse = authService.authenticate(login, password);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                login,
                                null,
                                Collections.singletonList(
                                        new SimpleGrantedAuthority("ROLE_" + authResponse.role())
                                )
                        );

                Map<String, Object> details = new HashMap<>();
                details.put("jwt", authResponse.token());
                details.put("userId", authResponse.userId());
                authentication.setDetails(details);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                Cookie cookie = new Cookie("JWT-TOKEN", authResponse.token());
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(3600);
                response.addCookie(cookie);
            } catch (Exception e) {
                log.warn("Auto-login failed: {}", e.getMessage());
                return "redirect:/login";
            }

            return "redirect:/";

        } catch (RegistrationException e) {
            log.error("Registration failed", e);

            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("login", login);
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("birthdate", birthdate);

            return "signup";
        }
    }
}