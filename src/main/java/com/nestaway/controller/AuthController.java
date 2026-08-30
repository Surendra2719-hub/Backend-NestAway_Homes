package com.nestaway.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nestaway.dto.LoginRequest;
import com.nestaway.dto.LoginResponse;
import com.nestaway.entity.User;
import com.nestaway.exception.ResourceNotFoundException;
import com.nestaway.repository.UserRepository;
import com.nestaway.config.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtUtil jwtUtil) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));

        } catch (BadCredentialsException exception) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token);
    }
}