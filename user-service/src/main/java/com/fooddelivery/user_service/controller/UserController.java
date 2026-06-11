package com.fooddelivery.user_service.controller;

import com.fooddelivery.user_service.dto.LoginRequest;
import com.fooddelivery.user_service.dto.UserRequest;
import com.fooddelivery.user_service.dto.UserResponse;
import com.fooddelivery.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserRequest userRequest) {
        log.info("Registering user email={}", userRequest.getEmail());
        return userService.registerUser(userRequest);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        log.info("Login attempt email={}", loginRequest.getEmail());
        return userService.login(loginRequest);
    }
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getUserById(@PathVariable String id) {
        log.debug("Fetching user id={}", id);
        return userService.getUserById(id);
    }

}
