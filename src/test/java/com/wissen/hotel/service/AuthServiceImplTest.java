package com.wissen.hotel.service;
import com.wissen.hotel.dto.request.*;
import com.wissen.hotel.enums.UserRole;
import com.wissen.hotel.exception.*;
import com.wissen.hotel.model.User;
import com.wissen.hotel.repository.UserRepository;
import com.wissen.hotel.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.wissen.hotel.service.impl.AuthServiceImpl;
import com.wissen.hotel.service.impl.EmailServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private AuthServiceImpl authService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private EmailServiceImpl emailSender;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtUtil = mock(JwtUtil.class);
        emailSender = mock(EmailServiceImpl.class);
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtUtil, emailSender);
    }

    @Test
    void testRegister_EmailAlreadyInUse() {
        RegisterRequest request = RegisterRequest.builder()
            .name("John Doe")
            .email("john.doe@example.com")
            .password("password123")
            .role(UserRole.CUSTOMER)
            .phone("1234567890")
            .dob(LocalDate.of(1990, 1, 1))
            .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        EmailAlreadyInUseException exception = assertThrows(EmailAlreadyInUseException.class, () -> authService.register(request));
        assertEquals("Email already in use", exception.getMessage());
    }

    @Test
    void testRegister_PhoneAlreadyInUse() {
        RegisterRequest request = RegisterRequest.builder()
            .name("John Doe")
            .email("john.doe@example.com")
            .password("password123")
            .role(UserRole.CUSTOMER)
            .phone("1234567890")
            .dob(LocalDate.of(1990, 1, 1))
            .build();

        when(userRepository.findByPhone(request.getPhone())).thenReturn(Optional.of(new User()));

        PhoneAlreadyInUseException exception = assertThrows(PhoneAlreadyInUseException.class, () -> authService.register(request));
        assertEquals("Phone number already in use", exception.getMessage());
    }

    @Test
    void testRegister_Success() {
        RegisterRequest request = RegisterRequest.builder()
            .name("John Doe")
            .email("john.doe@example.com")
            .password("password123")
            .role(UserRole.CUSTOMER) // 👈 Requested role
            .phone("1234567890")
            .dob(LocalDate.of(1990, 1, 1))
            .build();
    
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        
        // 👇 Mock the correct token generation method
        when(jwtUtil.generateEmailVerificationToken(request.getEmail()))
            .thenReturn("mockVerificationToken");
    
        authService.register(request);
    
        // 👇 Verify with corrected role check
        verify(userRepository).save(argThat(actualUser -> 
            actualUser.getEmail().equals(request.getEmail()) &&
            actualUser.getName().equals(request.getName()) &&
            actualUser.getRole() == UserRole.CUSTOMER // ✅ Matches request
        ));
        
        verify(emailSender).sendVerificationEmail(eq(request.getEmail()), eq("mockVerificationToken"));
    }
    

    @Test
    void testLogin_InvalidEmail() {
        LoginRequest request = new LoginRequest("invalid@example.com", "password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        assertEquals("Invalid email or password", exception.getMessage());
    }

        @Test
    void testResetPassword_InvalidPassword() {
        String token = "validToken";
        String email = "john.doe@example.com";
        String newPassword = "";
    
        User user = new User();
        user.setEmail(email);
    
        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(token, newPassword));
        assertEquals("New password cannot be null or empty", exception.getMessage());
    }

    @Test
    void testVerifyEmail_UserNotFound() {
        String token = "validToken";
        String email = "nonexistent@example.com";

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<String> response = authService.verifyEmail(token);

        assertEquals(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("User not found for email: " + email));
    }


    @Test
    void testResetPassword_Success() {
        String token = "validToken";
        String email = "john.doe@example.com";
        String newPassword = "newPassword123";
        User user = new User();
        user.setEmail(email);

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        authService.resetPassword(token, newPassword);

        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void testResetPassword_UserNotFound() {
        String token = "validToken";
        String email = "nonexistent@example.com";
        String newPassword = "newPassword123";

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> authService.resetPassword(token, newPassword));
        assertEquals("User not found for email: " + email, exception.getMessage());
    }

}