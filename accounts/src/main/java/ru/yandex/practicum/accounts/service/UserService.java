package ru.yandex.practicum.accounts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.accounts.dto.*;
import ru.yandex.practicum.accounts.exception.ValidationException;
import ru.yandex.practicum.accounts.model.User;
import ru.yandex.practicum.accounts.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationsClient notificationsClient;

    public UserInfoResponse getUserInfoByUserId(Long userId) {
        log.debug("Getting user info for userId: {}", userId);

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

    public UserInfoResponse getUserInfoByUsername(String username) {
        log.debug("Getting user info for username: {}", username);

        User user = userRepository.findByUsername(username)
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
        log.debug("Updating password for userId: {}", userId);

        List<String> errors = validatePasswordUpdate(request);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        log.debug("Password updated successfully for userId: {}", userId);

        notificationsClient.sendNotification(
                userId,
                "PASSWORD_CHANGE",
                "Your password has been changed successfully. If you didn't make this change, please contact support immediately."
        );
    }

    @Transactional
    public UserInfoResponse updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        log.debug("Updating user info for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setBirthdate(request.getBirthdate());

        User savedUser = userRepository.save(user);

        log.debug("User info updated successfully for userId: {}", userId);

        return new UserInfoResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getBirthdate(),
                savedUser.getRole()
        );
    }

    public List<UserListResponse> getAllUsers() {
        log.debug("Getting all users list");

        List<User> users = (List<User>) userRepository.findAll();

        return users.stream()
                .map(user -> new UserListResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getName()
                ))
                .collect(Collectors.toList());
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

        return errors;
    }
}