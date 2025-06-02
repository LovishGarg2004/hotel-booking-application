package com.wissen.hotel.services;

import org.springframework.http.ResponseEntity;

import com.wissen.hotel.dtos.*;

public interface AuthService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    ResponseEntity<String> verifyEmail(String token);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}

