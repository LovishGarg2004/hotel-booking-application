package com.wissen.hotel.service;

import org.springframework.http.ResponseEntity;

import com.wissen.hotel.dto.request.LoginRequest;
import com.wissen.hotel.dto.request.RegisterRequest;
import com.wissen.hotel.dto.response.LoginResponse;

public interface AuthService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    ResponseEntity<String> verifyEmail(String token);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}

