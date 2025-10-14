package ru.yandex.practicum.accounts.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.accounts.dto.UpdatePasswordRequest;
import ru.yandex.practicum.accounts.dto.UpdateUserInfoRequest;
import ru.yandex.practicum.accounts.dto.UserInfoResponse;
import ru.yandex.practicum.accounts.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoResponse> getUserInfo(
            @PathVariable Long userId,
            HttpServletRequest request) {

        Long authenticatedUserId = (Long) request.getAttribute("userId");

        if (!userId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("Access denied");
        }

        UserInfoResponse response = userService.getUserInfo(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @PathVariable Long userId,
            @RequestBody UpdatePasswordRequest request,
            HttpServletRequest httpRequest) {

        log.info("Password update request for user: {}", userId);

        Long authenticatedUserId = (Long) httpRequest.getAttribute("userId");

        if (!userId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("Access denied");
        }

        userService.updatePassword(userId, request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Пароль успешно изменен");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserInfoResponse> updateUserInfo(
            @PathVariable Long userId,
            @RequestBody UpdateUserInfoRequest request,
            HttpServletRequest httpRequest) {

        log.info("User info update request for user: {}", userId);

        Long authenticatedUserId = (Long) httpRequest.getAttribute("userId");

        if (!userId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("Access denied");
        }

        UserInfoResponse response = userService.updateUserInfo(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {

        log.info("User delete request for user: {}", userId);

        Long authenticatedUserId = (Long) httpRequest.getAttribute("userId");

        if (!userId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("Access denied");
        }

        userService.deleteUser(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Пользователь удален");
        return ResponseEntity.ok(response);
    }


}