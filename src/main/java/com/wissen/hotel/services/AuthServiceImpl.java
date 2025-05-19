package com.wissen.hotel.services;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.models.User;
import com.wissen.hotel.enums.UserRole;
import com.wissen.hotel.repositories.UserRepository;
import com.wissen.hotel.exceptions.*;
import com.wissen.hotel.utils.JwtUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            logger.error("Phone number already in use: {}", request.getPhone());
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

        // Generate JWT token
        logger.info("Generating JWT token for email: {}", request.getEmail());
        String token = jwtUtil.generateToken(user);

        logger.info("Login successful for email: {}", request.getEmail());
        return new LoginResponse(token, user.getRole().toString());
    }

    @Override
    public void verifyEmail(String token) {
        logger.info("Verifying email with token: {}", token);
    
        // Decode the token to extract the email
        String email = jwtUtil.extractEmail(token);
        logger.debug("Extracted email from token: {}", email);
    
        // Find the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
    
        logger.info("User found for email: {}", email);
    
        // Check if the email is already verified
        if (user.isEmailVerified()) {
            logger.warn("Email is already verified for user: {}", email);
            throw new EmailAlreadyVerifiedException("Email is already verified.");
        }
        
        //Here add the logic to verify the email
        //Send a verification email or update the user status in the database

        // Mark the email as verified
        user.setEmailVerified(true);
        userRepository.save(user);
    
        logger.info("Email verified successfully for user: {}", email);
    }

    @Override
    public void forgotPassword(String email) {
        logger.warn("Forgot password functionality is no longer supported.");
        throw new UnsupportedOperationException("Forgot password functionality is no longer supported.");
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        logger.info("Resetting password with token: {}", token);
    
        // Decode the token to extract the email
        String email = jwtUtil.extractEmail(token);
        logger.debug("Extracted email from token: {}", email);
    
        // Find the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
    
        logger.info("User found for email: {}", email);
    
        // Validate the new password (optional, based on your requirements)
        if (newPassword == null || newPassword.isEmpty()) {
            logger.error("New password is invalid for email: {}", email);
            throw new IllegalArgumentException("New password cannot be null or empty");
        }
        // Encode the new password and update the user
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    
        logger.info("Password reset successfully for user: {}", email);
    }
}

