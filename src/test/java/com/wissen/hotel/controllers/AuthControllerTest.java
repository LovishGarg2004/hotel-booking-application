package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister() {
        RegisterRequest request = new RegisterRequest();
        // Set up request fields as needed

        doNothing().when(authService).register(request);

        ResponseEntity<Void> response = authController.register(request);

        assertEquals(200, response.getStatusCodeValue());
        verify(authService, times(1)).register(request);
    }

    @Test
    void testLogin() {
        LoginRequest request = new LoginRequest();
        // Set up request fields as needed
        LoginResponse mockResponse = new LoginResponse();

        when(authService.login(request)).thenReturn(mockResponse);

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());
        verify(authService, times(1)).login(request);
    }

    @Test
    void testVerifyEmail() {
        String token = "sampleToken";

        doNothing().when(authService).verifyEmail(token);

        ResponseEntity<Void> response = authController.verifyEmail(token);

        assertEquals(200, response.getStatusCodeValue());
        verify(authService, times(1)).verifyEmail(token);
    }

    @Test
    void testForgotPassword() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        doNothing().when(authService).forgotPassword(request.getEmail());

        ResponseEntity<Void> response = authController.forgotPassword(request);

        assertEquals(200, response.getStatusCodeValue());
        verify(authService, times(1)).forgotPassword(request.getEmail());
    }

    @Test
    void testResetPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("sampleToken");
        request.setNewPassword("newPassword");

        doNothing().when(authService).resetPassword(request.getToken(), request.getNewPassword());

        ResponseEntity<Void> response = authController.resetPassword(request);

        assertEquals(200, response.getStatusCodeValue());
        verify(authService, times(1)).resetPassword(request.getToken(), request.getNewPassword());
    }
}