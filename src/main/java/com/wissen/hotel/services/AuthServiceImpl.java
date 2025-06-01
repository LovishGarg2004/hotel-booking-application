package com.wissen.hotel.services;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.enums.UserRole;
import com.wissen.hotel.exceptions.*;
import com.wissen.hotel.models.User;
import com.wissen.hotel.repositories.UserRepository;
import com.wissen.hotel.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailSender emailSender;

    @Value("${app.reset-password-url:}")
    private String resetPasswordUrl;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailSender emailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailSender = emailSender;
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("Email already in use");
        }
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new PhoneAlreadyInUseException("Phone number already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.valueOf(request.getRole().toString().toUpperCase()));
        user.setPhone(request.getPhone());
        user.setDob(request.getDob());
        user.setCreatedAt(LocalDateTime.now());
        user.setEmailVerified(false);

        String token = jwtUtil.generateEmailVerificationToken(user.getEmail());
        emailSender.sendVerificationEmail(user.getEmail(), token);
        userRepository.save(user);

        if (user.getRole() == UserRole.HOTEL_OWNER) {
            try {
                emailSender.sendWelcomeEmail(user.getEmail(), user.getName());
            } catch (Exception ignored) {
                // Continue with registration even if welcome email fails
            }
        }
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Uncomment if you want to enforce email verification
        // if (!user.isEmailVerified()) {
        //     throw new EmailNotVerifiedException("Please verify your email before logging in");
        // }

        String token = jwtUtil.generateToken(user);
        return new LoginResponse(token, user.getRole().toString());
    }

    @Override
    public void verifyEmail(String token) {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email is already verified.");
        }
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(String email) {
        throw new UnsupportedOperationException("Forgot password functionality is no longer supported.");
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));

        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("New password cannot be null or empty");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
