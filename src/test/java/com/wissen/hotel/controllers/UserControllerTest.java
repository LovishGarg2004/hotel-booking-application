package com.wissen.hotel.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wissen.hotel.dtos.*;
import com.wissen.hotel.enums.UserRole;
import com.wissen.hotel.services.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetCurrentUser() throws Exception {
        // Mock response
        UserResponse response = new UserResponse();
        response.setName("Saloni Gupta");
        response.setPhone("1234567890");
        response.setDob(LocalDate.of(2004, 10, 19));
        response.setRole(UserRole.ADMIN);
        response.setId("190b8c4e-0f3d-4a2b-bc5f-1a2b3c4d5e6f");
        response.setEmail("saloni@gmail.com");

        Mockito.when(userService.getCurrentUser()).thenReturn(response);

        // Perform GET request and validate response
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("saloni@gmail.com"))
                .andExpect(jsonPath("$.name").value("Saloni Gupta"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void testUpdateCurrentUser() throws Exception {
        // Mock request and response
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Saloni");
        updateRequest.setPhone("1234567890");
        updateRequest.setDob(LocalDate.of(2004, 10, 19));

        UserResponse response = new UserResponse();
        response.setName("Updated Saloni");
        response.setPhone("1234567890");
        response.setDob(LocalDate.of(2004, 10, 19));

        Mockito.when(userService.updateCurrentUser(Mockito.any(UpdateUserRequest.class)))
                .thenReturn(response);

        // Perform PUT request and validate response
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Saloni"))
                .andExpect(jsonPath("$.phone").value("1234567890"));
    }

    @Test
        void testGetCurrentUserBookings() throws Exception {
        // Mock response
        BookingResponse bookingResponse = BookingResponse.builder()
                .bookingId(UUID.randomUUID())
                .roomId(UUID.randomUUID())
                .checkIn(LocalDate.of(2025, 5, 20))
                .checkOut(LocalDate.of(2025, 5, 25))
                .build();

        Mockito.when(userService.getCurrentUserBookings())
                .thenReturn(List.of(bookingResponse));

        // Perform GET request and validate response
        mockMvc.perform(get("/api/users/me/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value(bookingResponse.getBookingId().toString()))
                .andExpect(jsonPath("$[0].roomId").value(bookingResponse.getRoomId().toString()))
                .andExpect(jsonPath("$[0].checkIn").value("2025-05-20"))
                .andExpect(jsonPath("$[0].checkOut").value("2025-05-25"));
        }

    @Test
    void testGetUserById() throws Exception {
        // Mock response
        UUID id = UUID.randomUUID();
        UserResponse response = new UserResponse();
        response.setEmail("admin@example.com");
        response.setName("Admin User");

        Mockito.when(userService.getUserById(id.toString()))
                .thenReturn(response);

        // Perform GET request and validate response
        mockMvc.perform(get("/api/users/admin/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.name").value("Admin User"));
    }

    @Test
    void testGetAllUsers() throws Exception {
        // Mock response
        UserResponse userResponse = new UserResponse();
        userResponse.setId(UUID.randomUUID().toString());
        userResponse.setName("User 1");
        userResponse.setEmail("user1@example.com");

        Mockito.when(userService.getAllUsers(0, 10))
                .thenReturn(List.of(userResponse));

        // Perform GET request and validate response
        mockMvc.perform(get("/api/users/admin/usersall")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("User 1"))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"));
    }

        @Test
        void testUpdateUserRole() throws Exception {
        // Mock request
        UUID id = UUID.randomUUID();
        String role = "ADMIN";

        // Perform PUT request and validate response
        mockMvc.perform(put("/api/users/admin/{id}/role", id)
                        .param("role", role)) // Pass the role as a query parameter
                .andExpect(status().isOk()); // Expect HTTP 200 OK status

        // Verify that the service method was called with the correct arguments
        Mockito.verify(userService).updateUserRole(
                Mockito.eq(id.toString()), // Verify the user ID
                Mockito.argThat(request -> request.getRole() == UserRole.ADMIN) // Verify the role in the request
        );
        }
    @Test
    void testDeleteUser() throws Exception {
        // Mock request
        UUID id = UUID.randomUUID();

        // Perform DELETE request and validate response
        mockMvc.perform(delete("/api/users/admin/{id}", id))
                .andExpect(status().isNoContent());

        // Verify service call
        Mockito.verify(userService).deleteUser(id.toString());
    }
}