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
        log.info("Registering new user: {}", request.getLogin());

        // Валидация
        List<String> errors = validateRegistration(request);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        // Проверка существования пользователя
        if (userRepository.findByUsername(request.getLogin()).isPresent()) {
            throw new ValidationException(List.of("Пользователь с таким логином уже существует"));
        }

        // Создание пользователя
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

        log.info("User registered successfully: {}", savedUser.getUsername());

        return new RegistrationResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                "Регистрация прошла успешно"
        );
    }

    private List<String> validateRegistration(RegistrationRequest request) {
        List<String> errors = new ArrayList<>();

        // Проверка заполнения полей
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

        // Проверка совпадения паролей
        if (request.getPassword() != null && request.getConfirmPassword() != null) {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                errors.add("Пароли не совпадают");
            }
        }

        // Проверка возраста (старше 18 лет)
        if (request.getBirthdate() != null) {
            int age = Period.between(request.getBirthdate(), LocalDate.now()).getYears();
            if (age < 18) {
                errors.add("Возраст должен быть не менее 18 лет");
            }
        }

        // Проверка длины логина
        if (request.getLogin() != null && request.getLogin().length() < 3) {
            errors.add("Логин должен содержать минимум 3 символа");
        }

        // Проверка длины пароля
        if (request.getPassword() != null && request.getPassword().length() < 4) {
            errors.add("Пароль должен содержать минимум 4 символа");
        }

        // Проверка email (если указан)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                errors.add("Некорректный формат email");
            }
        }

        return errors;
    }
}