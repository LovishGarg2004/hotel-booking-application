package com.wissen.hotel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import com.wissen.hotel.dto.request.LoginRequest;
import com.wissen.hotel.dto.request.RegisterRequest;
import com.wissen.hotel.dto.response.LoginResponse;
import com.wissen.hotel.enums.UserRole;
import com.wissen.hotel.exception.*;
import com.wissen.hotel.model.User;
import com.wissen.hotel.repository.UserRepository;
import com.wissen.hotel.service.impl.AuthServiceImpl;
import com.wissen.hotel.util.JwtUtil;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private EmailService emailSender;
    
    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(UserRole.CUSTOMER);
        testUser.setEmailVerified(false);
        
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password");
        registerRequest.setRole(UserRole.CUSTOMER);
        
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");
    }

    // In registration test
    @Test
    void register_ShouldSetAllUserFieldsCorrectly() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(any())).thenReturn(Optional.empty());
        
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setPhone("1234567890");
        request.setDob(LocalDate.of(1990, 1, 1));
        request.setRole(UserRole.CUSTOMER);
        
        authService.register(request);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertEquals("Test User", savedUser.getName());
        assertEquals("1234567890", savedUser.getPhone());
        assertEquals(UserRole.CUSTOMER, savedUser.getRole());
        assertNotNull(savedUser.getCreatedAt());
    }


    // Registration Tests
    @Test
    void register_ShouldSaveUserAndSendVerificationEmail() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        
        authService.register(registerRequest);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(emailSender).sendVerificationEmail(any(), any());
        
        User savedUser = userCaptor.getValue();
        assertEquals("new@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
    }

    @Test
    void register_ShouldThrowEmailAlreadyExists() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User()));
        assertThrows(EmailAlreadyInUseException.class, () -> authService.register(registerRequest));
    }

    @Test
    void register_ShouldThrowPhoneAlreadyInUse() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(any())).thenReturn(Optional.of(new User()));
        
        RegisterRequest request = new RegisterRequest();
        request.setPhone("1234567890"); // Conflict trigger
        
        assertThrows(PhoneAlreadyInUseException.class, 
            () -> authService.register(request));
    }


    @Test
    void resetPassword_ShouldThrowForInvalidUser() {
        when(jwtUtil.extractEmail(any())).thenReturn("missing@example.com");
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        
        assertThrows(UserNotFoundException.class, 
            () -> authService.resetPassword("token", "newPass"));
    }


    // Login Tests
    @Test
    void login_ShouldReturnTokenForValidCredentials() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtUtil.generateToken(any())).thenReturn("testToken");
        
        LoginResponse response = authService.login(loginRequest);
        
        assertEquals("testToken", response.getToken());
    }

    @Test
    void login_ShouldThrowForInvalidPassword() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        
        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    // Email Verification Tests
    @Test
    void verifyEmail_ShouldActivateUserAndSendWelcomeEmail() {
        when(jwtUtil.extractEmail(any())).thenReturn("test@example.com");
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        
        ResponseEntity<String> response = authService.verifyEmail("validToken");
        
        assertTrue(testUser.isEmailVerified());
        verify(emailSender).sendWelcomeEmail(any(), any(), any());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void verifyEmail_ShouldReturnErrorResponseForAlreadyVerified() {
        testUser.setEmailVerified(true);
        when(jwtUtil.extractEmail(any())).thenReturn("test@example.com");
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));

        ResponseEntity<String> response = authService.verifyEmail("token");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Email is already verified."));
    }


    @Test
    void verifyEmail_ShouldReturnHotelOwnerMessage() {
        testUser.setRole(UserRole.HOTEL_OWNER);
        when(jwtUtil.extractEmail(any())).thenReturn("test@example.com");
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        
        ResponseEntity<String> response = authService.verifyEmail("token");
        assertTrue(response.getBody().contains("hotel owner"));
    }

    @Test
    void verifyEmail_ShouldReturnDefaultUserMessage() {
        testUser.setRole(UserRole.CUSTOMER);
        when(jwtUtil.extractEmail(any())).thenReturn("test@example.com");
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        
        ResponseEntity<String> response = authService.verifyEmail("token");
        assertTrue(response.getBody().contains("Welcome to Hotel Booking"));
    }

    @Test
    void verifyEmail_ShouldReturnErrorResponseForUserNotFound() {
        when(jwtUtil.extractEmail(any())).thenReturn("missing@example.com");
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        ResponseEntity<String> response = authService.verifyEmail("validToken");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("User not found for email"));
    }



    // Password Reset Tests
    @Test
    void resetPassword_ShouldUpdatePassword() {
        when(jwtUtil.extractEmail(any())).thenReturn("test@example.com");
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(any())).thenReturn("newEncodedPassword");
        
        authService.resetPassword("validToken", "newPassword");
        
        assertEquals("newEncodedPassword", testUser.getPassword());
        verify(userRepository).save(testUser);
    }

    @Test
    void resetPassword_ShouldThrowForEmptyPassword() {
        when(jwtUtil.extractEmail(any())).thenReturn("test@example.com");
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.resetPassword("validToken", ""));
        assertEquals("New password cannot be null or empty", exception.getMessage());
    }

    // Forgot Password Test
    @Test
    void forgotPassword_ShouldThrowUnsupportedOperation() {
        assertThrows(UnsupportedOperationException.class, 
            () -> authService.forgotPassword("test@example.com"));
    }

    // Additional Test Cases
    @Test
    void verifyEmail_ShouldHandleInvalidToken() {
        when(jwtUtil.extractEmail(any())).thenThrow(new IllegalArgumentException("Invalid token"));
        
        ResponseEntity<String> response = authService.verifyEmail("invalidToken");
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid token"));
    }

    @Test
    void login_ShouldThrowForNonExistentUser() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }
}
