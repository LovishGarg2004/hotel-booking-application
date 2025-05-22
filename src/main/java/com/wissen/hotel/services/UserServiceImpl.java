package com.wissen.hotel.services;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.models.Booking;
import com.wissen.hotel.models.User;
import com.wissen.hotel.repositories.BookingRepository;
import com.wissen.hotel.repositories.UserRepository;
import com.wissen.hotel.utils.AuthUtil;

import jakarta.transaction.Transactional;

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
    @Autowired
    private BookingRepository bookingRepository;

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
    @Transactional
    public List<BookingResponse> getCurrentUserBookings() {
        logger.info("Fetching bookings for current user");

        // Step 1: Get the current authenticated user
        User currentUser = AuthUtil.getCurrentUser();

        // Step 2: Fetch all bookings made by the user
        List<Booking> bookings = bookingRepository.findAllByUser_UserId(currentUser.getUserId());

        // Step 3: Map to DTOs
        List<BookingResponse> responses = bookings.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        logger.info("Fetched {} bookings for user: {}", responses.size(), currentUser.getEmail());
        return responses;
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

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUser().getUserId())
                .roomId(booking.getRoom().getRoomId())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .guests(booking.getGuests())
                .finalPrice(booking.getFinalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
