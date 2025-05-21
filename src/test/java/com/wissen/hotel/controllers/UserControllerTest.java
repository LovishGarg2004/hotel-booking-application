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
        UserResponse response = new UserResponse();
        response.setName("Saloni Gupta");
        response.setPhone("1234567890");
        response.setDob(LocalDate.of(2004, 10, 19));
        response.setRole(UserRole.ADMIN);
        response.setId("190b8c4e-0f3d-4a2b-bc5f-1a2b3c4d5e6f");
        response.setEmail("saloni@gmail.com");        

        Mockito.when(userService.getCurrentUser()).thenReturn(response);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("saloni@gmail.com"));
    }

    @Test
    void testUpdateCurrentUser() throws Exception {
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

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Saloni"));
    }

    @Test
    void testGetCurrentUserBookings() throws Exception {
        Mockito.when(userService.getCurrentUserBookings())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users/me/bookings"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetUserById() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse response = new UserResponse();
        response.setEmail("admin@example.com");

        Mockito.when(userService.getUserById(id.toString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/users/admin/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@example.com"));
    }

    @Test
    void testGetAllUsers() throws Exception {
        Mockito.when(userService.getAllUsers(0, 10))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users/admin/usersall"))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateUserRole() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/users/admin/{id}/role", id)
                        .param("role", "ADMIN"))
                .andExpect(status().isOk());

        Mockito.verify(userService).updateUserRole(Mockito.eq(id.toString()), Mockito.any(UpdateUserRoleRequest.class));
    }

    @Test
    void testDeleteUser() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/users/admin/{id}", id))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).deleteUser(id.toString());
    }
}
