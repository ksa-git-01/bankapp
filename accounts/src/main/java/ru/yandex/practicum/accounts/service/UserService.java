package ru.yandex.practicum.accounts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.accounts.dto.UpdatePasswordRequest;
import ru.yandex.practicum.accounts.dto.UpdateUserInfoRequest;
import ru.yandex.practicum.accounts.dto.UserInfoResponse;
import ru.yandex.practicum.accounts.exception.ValidationException;
import ru.yandex.practicum.accounts.model.User;
import ru.yandex.practicum.accounts.repository.UserRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getBirthdate(),
                user.getRole()
        );
    }

    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        log.info("Updating password for user: {}", userId);

        List<String> errors = validatePasswordUpdate(request);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        log.info("Password updated successfully for user: {}", userId);
    }

    @Transactional
    public UserInfoResponse updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        log.info("Updating user info for user: {}", userId);

        List<String> errors = validateUserInfoUpdate(request);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setBirthdate(request.getBirthdate());

        User savedUser = userRepository.save(user);

        log.info("User info updated successfully for user: {}", userId);

        return new UserInfoResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getBirthdate(),
                savedUser.getRole()
        );
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);

        // TODO: Проверить что у пользователя нет ненулевых счетов
        // Это нужно реализовать когда появится функциональность счетов

        userRepository.deleteById(userId);

        log.info("User deleted successfully: {}", userId);
    }

    private List<String> validatePasswordUpdate(UpdatePasswordRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            errors.add("Пароль обязателен для заполнения");
        }

        if (request.getConfirmPassword() == null || request.getConfirmPassword().isEmpty()) {
            errors.add("Подтверждение пароля обязательно");
        }

        if (request.getPassword() != null && request.getConfirmPassword() != null) {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                errors.add("Пароли не совпадают");
            }
        }

        if (request.getPassword() != null && request.getPassword().length() < 4) {
            errors.add("Пароль должен содержать минимум 4 символа");
        }

        return errors;
    }

    private List<String> validateUserInfoUpdate(UpdateUserInfoRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            errors.add("Имя обязательно для заполнения");
        }

        if (request.getBirthdate() == null) {
            errors.add("Дата рождения обязательна для заполнения");
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