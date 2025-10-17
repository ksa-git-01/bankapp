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
import ru.yandex.practicum.accounts.dto.UserListResponse;
import ru.yandex.practicum.accounts.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/id/{userId}")
    public ResponseEntity<UserInfoResponse> getUserInfoByUserId(
            @PathVariable Long userId,
            HttpServletRequest request) {

        Long authenticatedUserId = (Long) request.getAttribute("userId");

        if (!userId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("Access denied");
        }

        UserInfoResponse response = userService.getUserInfoByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserInfoResponse> getUserInfoByUsername(
            @PathVariable String username) {
        UserInfoResponse response = userService.getUserInfoByUsername(username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @PathVariable Long userId,
            @RequestBody UpdatePasswordRequest request,
            HttpServletRequest httpRequest) {

        log.debug("Password update request for user: {}", userId);

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

        log.debug("User info update request for user: {}", userId);

        Long authenticatedUserId = (Long) httpRequest.getAttribute("userId");

        if (!userId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("Access denied");
        }

        UserInfoResponse response = userService.updateUserInfo(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserListResponse>> getAllUsers() {
        log.debug("Getting all users list");

        List<UserListResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}