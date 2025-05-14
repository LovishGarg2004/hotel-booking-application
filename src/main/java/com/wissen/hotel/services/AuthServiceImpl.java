package com.wissen.hotel.services;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.models.User;
import com.wissen.hotel.enums.UserRole;
import com.wissen.hotel.repositories.UserRepository;
import com.wissen.hotel.exceptions.*;
import com.wissen.hotel.utils.JwtUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.reset-password-url}")
    private String resetPasswordUrl;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void register(RegisterRequest request) {
        logger.info("Registering user with email: {}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.error("Email already in use: {}", request.getEmail());
            throw new EmailAlreadyInUseException("Email already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.valueOf(request.getRole().toString().toUpperCase()));
        user.setPhone(request.getPhone());
        user.setDob(request.getDob());
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        logger.info("User registered successfully with email: {}", request.getEmail());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        logger.info("Attempting login for email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.error("Invalid email or password for email: {}", request.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.error("Invalid password for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user);
        logger.info("Login successful for email: {}", request.getEmail());
        return new LoginResponse(token, user.getRole().toString());
    }

    @Override
    public void verifyEmail(String token) {
        logger.warn("Email verification is no longer supported.");
        throw new UnsupportedOperationException("Email verification is no longer supported.");
    }

    @Override
    public void forgotPassword(String email) {
        logger.warn("Forgot password functionality is no longer supported.");
        throw new UnsupportedOperationException("Forgot password functionality is no longer supported.");
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        logger.warn("Password reset functionality is no longer supported.");
        throw new UnsupportedOperationException("Password reset functionality is no longer supported.");
    }
}

