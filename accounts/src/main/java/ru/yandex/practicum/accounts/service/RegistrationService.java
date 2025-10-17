package ru.yandex.practicum.accounts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.accounts.dto.RegistrationRequest;
import ru.yandex.practicum.accounts.dto.RegistrationResponse;
import ru.yandex.practicum.accounts.exception.ValidationException;
import ru.yandex.practicum.accounts.model.User;
import ru.yandex.practicum.accounts.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegistrationResponse registerUser(RegistrationRequest request) {
        log.debug("Registering new user: {}", request.getLogin());

        List<String> errors = validateRegistration(request);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        if (userRepository.findByUsername(request.getLogin()).isPresent()) {
            throw new ValidationException(List.of("Пользователь с таким логином уже существует"));
        }

        User user = new User();
        user.setUsername(request.getLogin());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setBirthdate(request.getBirthdate());
        user.setRole("USER");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        log.debug("User registered successfully: {}", savedUser.getUsername());

        return new RegistrationResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                "Регистрация прошла успешно"
        );
    }

    private List<String> validateRegistration(RegistrationRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getLogin() == null || request.getLogin().trim().isEmpty()) {
            errors.add("Логин обязателен для заполнения");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            errors.add("Пароль обязателен для заполнения");
        }

        if (request.getConfirmPassword() == null || request.getConfirmPassword().isEmpty()) {
            errors.add("Подтверждение пароля обязательно");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            errors.add("Имя обязательно для заполнения");
        }

        if (request.getBirthdate() == null) {
            errors.add("Дата рождения обязательна для заполнения");
        }

        if (request.getPassword() != null && request.getConfirmPassword() != null) {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                errors.add("Пароли не совпадают");
            }
        }

        if (request.getBirthdate() != null) {
            int age = Period.between(request.getBirthdate(), LocalDate.now()).getYears();
            if (age < 18) {
                errors.add("Возраст должен быть не менее 18 лет");
            }
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                errors.add("Некорректный формат email");
            }
        }

        return errors;
    }
}