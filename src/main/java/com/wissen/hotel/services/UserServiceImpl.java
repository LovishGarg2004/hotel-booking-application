// File: UserServiceImpl.java
package com.wissen.hotel.services;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.models.User;
import com.wissen.hotel.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserResponse getCurrentUser() {
        User user = (User) org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        if (user == null) {
            throw new RuntimeException("Unauthorized");
        }
        return UserResponse.from(user);
    }

    @Override
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        User user = (User) org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        if (user == null) {
            throw new RuntimeException("Unauthorized");
        }

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setDob(request.getDob());

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public List<BookingResponse> getCurrentUserBookings() {
        // Mock response – integrate with BookingService later
        return List.of();
    }

    @Override
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.from(user);
    }

    @Override
    public List<UserResponse> getAllUsers(int page, int size) {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public void updateUserRole(String id, UpdateUserRoleRequest request) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(request.getRole());
        userRepository.save(user);
    }

    @Override
    public void deleteUser(String id) {
        userRepository.deleteById(UUID.fromString(id));
    }
}
