package com.wissen.hotel.controller;

import com.wissen.hotel.dto.request.RegisterRequest;
import com.wissen.hotel.dto.request.LoginRequest;
import com.wissen.hotel.dto.request.ForgotPasswordRequest;
import com.wissen.hotel.dto.request.ResetPasswordRequest;
import com.wissen.hotel.dto.response.LoginResponse;
import com.wissen.hotel.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for authentication and account management")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return a token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    //Jashit
    @GetMapping("/verify/{token}")
    @Operation(summary = "Verify Email", description = "Verify user account via token sent on email")
    public ResponseEntity<String> verifyEmail(@PathVariable String token) {
        return ResponseEntity.ok("Email verified successfully!");
    }

    //Jashit
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot Password", description = "Send password reset link to user email")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password", description = "Reset password using token")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
