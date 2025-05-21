package com.wissen.hotel.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wissen.hotel.dtos.*;
import com.wissen.hotel.enums.UserRole;
import com.wissen.hotel.services.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegister() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("saloni@gmail.com");
        request.setPassword("Password123");
        request.setName("Saloni Gupta");
        request.setPhone("1234567890");
        request.setRole(UserRole.ADMIN);
        request.setDob(LocalDate.of(2004, 10, 19));

        doNothing().when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("saloni@gmail.com");
        request.setPassword("Password123");

        LoginResponse response = new LoginResponse();
        response.setToken("sample-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("sample-token"));
    }

    @Test
    void testVerifyEmail() throws Exception {
        String token = "dummy-token";

        doNothing().when(authService).verifyEmail(token);

        mockMvc.perform(get("/api/auth/verify/" + token))
                .andExpect(status().isOk());
    }

    @Test
    void testForgotPassword() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("saloni@gmail.com");

        doNothing().when(authService).forgotPassword(request.getEmail());

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testResetPassword() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token");
        request.setNewPassword("NewPassword123");

        doNothing().when(authService).resetPassword(request.getToken(), request.getNewPassword());

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
