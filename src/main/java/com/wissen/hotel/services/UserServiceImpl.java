package com.wissen.hotel.services;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.models.User;
import com.wissen.hotel.repositories.UserRepository;
import com.wissen.hotel.utils.AuthUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserResponse getCurrentUser() {
        logger.info("Fetching current user");
        User user = AuthUtil.getCurrentUser();
        logger.info("Current user fetched successfully: {}", user.getEmail());
        return UserResponse.from(user);
    }

    @Override
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        logger.info("Updating current user");
        User user = AuthUtil.getCurrentUser();

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setDob(request.getDob());

        logger.info("User updated successfully: {}", user.getEmail());
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public List<BookingResponse> getCurrentUserBookings() {
        logger.info("Fetching bookings for current user");
        User user = AuthUtil.getCurrentUser();
        // Placeholder for booking logic
        logger.info("Fetched bookings for user: {}", user.getEmail());
        return List.of(); // Replace with actual booking fetch
    }

    @Override
    public UserResponse getUserById(String id) {
        logger.info("Fetching user by ID: {}", id);
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new RuntimeException("User not found");
                });
        logger.info("User fetched successfully: {}", user.getEmail());
        return UserResponse.from(user);
    }

    @Override
    public List<UserResponse> getAllUsers(int page, int size) {
        logger.info("Fetching all users with pagination - page: {}, size: {}", page, size);
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
        logger.info("Total users fetched: {}", users.size());
        return users;
    }

    @Override
    public void updateUserRole(String id, UpdateUserRoleRequest request) {
        logger.info("Updating role for user ID: {}", id);
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new RuntimeException("User not found");
                });
        user.setRole(request.getRole());
        userRepository.save(user);
        logger.info("User role updated successfully for ID: {}", id);
    }

    @Override
    public void deleteUser(String id) {
        logger.info("Deleting user with ID: {}", id);
        userRepository.deleteById(UUID.fromString(id));
        logger.info("User deleted successfully with ID: {}", id);
    }
}
