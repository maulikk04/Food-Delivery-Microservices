package com.fooddelivery.user_service.service;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.common.exception.UnauthorizedException;
import com.fooddelivery.user_service.dto.LoginRequest;
import com.fooddelivery.user_service.dto.UserRequest;
import com.fooddelivery.user_service.dto.UserResponse;
import com.fooddelivery.user_service.model.User;
import com.fooddelivery.user_service.repository.UserRepository;
import com.fooddelivery.user_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    public UserResponse registerUser(UserRequest userRequest){
        User user = User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .role(userRequest.getRole() != null ? userRequest.getRole() : "ROLE_USER")
                .build();
        User savedUser = userRepository.save(user);
        log.info("User registered id={}", savedUser.getId());
        return mapToUserResponse(savedUser);
    }
    public String login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return jwtUtil.generateToken(user.getId(),user.getEmail(),user.getRole());
        }
        throw new UnauthorizedException("Invalid credentials");
    }
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }
    private UserResponse mapToUserResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
