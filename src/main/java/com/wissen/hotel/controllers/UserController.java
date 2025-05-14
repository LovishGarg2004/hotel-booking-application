package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.UserResponse;
import com.wissen.hotel.enums.UserRole;
import com.wissen.hotel.dtos.UpdateUserRequest;
import com.wissen.hotel.services.UserService;
import com.wissen.hotel.dtos.UpdateUserRoleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing user accounts and roles")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user profile", description = "Returns details of the currently authenticated user")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @Operation(summary = "Update current user profile", description = "Updates the profile details of the authenticated user")
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(@Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateCurrentUser(request));
    }

    @Operation(summary = "Get current user's bookings", description = "Returns a list of bookings made by the current user")
    @GetMapping("/me/bookings")
    public ResponseEntity<List<?>> getCurrentUserBookings() {
        return ResponseEntity.ok(userService.getCurrentUserBookings());
    }

    @Operation(summary = "Get user by ID", description = "Fetches details of a user by their UUID (Admin only)")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id.toString()));
    }

    @Operation(summary = "Get all users", description = "Returns a paginated list of all users (Admin only)")
    @GetMapping("/usersall")
    public ResponseEntity<List<UserResponse>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @Operation(summary = "Update user role", description = "Updates the role of a user by their ID (Admin only)")
    @PutMapping("/{id}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable UUID id, @RequestParam String role) {
        UserRole userRole;
        switch (role.toUpperCase()) {
            case "ADMIN":
            userRole = UserRole.ADMIN;
            break;
            case "USER":
            userRole = UserRole.CUSTOMER;
            break;
            case "MANAGER":
            userRole = UserRole.HOTEL_OWNER;
            break;
            default:
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        userService.updateUserRole(id.toString(), new UpdateUserRoleRequest(userRole));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete user", description = "Deletes a user by ID (Admin only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id.toString());
        return ResponseEntity.noContent().build();
    }
}
