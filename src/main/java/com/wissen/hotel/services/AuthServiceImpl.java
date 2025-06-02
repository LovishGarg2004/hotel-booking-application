package com.wissen.hotel.services;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.enums.UserRole;
import com.wissen.hotel.exceptions.*;
import com.wissen.hotel.models.User;
import com.wissen.hotel.repositories.UserRepository;
import com.wissen.hotel.utils.JwtUtil;
import com.wissen.hotel.services.EmailServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailSender;

    @Value("${app.reset-password-url:}")
    private String resetPasswordUrl;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailService emailSender) {
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
        userRepository.save(user);
        emailSender.sendVerificationEmail(user.getEmail(), token);
        
        logger.info("User registered successfully with email: {}", request.getEmail());
        
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

            if (!user.isEmailVerified()) {
                logger.warn("Login attempt with unverified email: {}", request.getEmail());
                throw new EmailNotVerifiedException("Please verify your email before logging in");
            }

        String token = jwtUtil.generateToken(user);
        return new LoginResponse(token, user.getRole().toString());
    }

    @Override
    public ResponseEntity<String> verifyEmail(String token) {
        try {
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
            // Mark the email as verified
            user.setEmailVerified(true);
            userRepository.save(user);
            logger.info("Email verified successfully for user: {}", email);
            //Welcome email after verification
            logger.info("Sending welcome email to user: {}", email);
            emailSender.sendWelcomeEmail(email, user.getName(), user.getRole());
            if (user.getRole() == UserRole.HOTEL_OWNER) {
                return ResponseEntity.ok("Email verified successfully! Welcome, hotel owner! You can now list your properties.");
            } else {
                return ResponseEntity.ok("Email verified successfully! Welcome to Hotel Booking Platform!");
            }
        } catch (Exception ex) {
            logger.error("Error verifying email: {}", ex.getMessage(), ex);
            return ResponseEntity
                .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred: " + ex.getMessage());
        }
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
