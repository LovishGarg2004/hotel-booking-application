package com.wissen.hotel.services;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.models.Booking;
import com.wissen.hotel.models.User;
import com.wissen.hotel.repositories.BookingRepository;
import com.wissen.hotel.repositories.UserRepository;
import com.wissen.hotel.utils.AuthUtil;
import com.wissen.hotel.exceptions.UserServiceException;

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

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public UserResponse getCurrentUser() {
        try {
            logger.info("Fetching current user");
            User user = AuthUtil.getCurrentUser();
            logger.info("Current user fetched successfully: {}", user.getEmail());
            return UserResponse.from(user); // includes emailVerified
        } catch (Exception e) {
            logger.error("Error fetching current user: {}", e.getMessage());
            throw new UserServiceException("Unable to fetch current user. Please login again.", e);
        }
    }

    @Override
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        try {
            logger.info("Updating current user");
            User user = AuthUtil.getCurrentUser();

            user.setName(request.getName());
            user.setPhone(request.getPhone());
            user.setDob(request.getDob());

            logger.info("User updated successfully: {}", user.getEmail());
            return UserResponse.from(userRepository.save(user)); // includes emailVerified
        } catch (Exception e) {
            logger.error("Error updating current user: {}", e.getMessage());
            throw new UserServiceException("Unable to update user profile. Please try again later.", e);
        }
    }

    @Override
    @Transactional
    public List<BookingResponse> getCurrentUserBookings() {
        try {
            logger.info("Fetching bookings for current user");

            // Step 1: Get the current authenticated user
            User currentUser = AuthUtil.getCurrentUser();

            // Step 2: Fetch all bookings made by the user
            List<Booking> bookings = bookingRepository.findAllByUser_UserId(currentUser.getUserId());

            // Step 3: Map to DTOs
            List<BookingResponse> responses = bookings.stream()
                .map(this::mapToResponse)
                .toList();

            logger.info("Fetched {} bookings for user: {}", responses.size(), currentUser.getEmail());
            return responses;
        } catch (Exception e) {
            logger.error("Error fetching bookings for current user: {}", e.getMessage());
            throw new UserServiceException("Unable to fetch your bookings. Please try again later.", e);
        }
    }

    @Override
    public UserResponse getUserById(String id) {
        try {
            logger.info("Fetching user by ID: {}", id);
            User user = userRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> {
                        logger.error("User not found with ID: {}", id);
                        return new UserServiceException("User not found with the provided ID.");
                    });
            logger.info("User fetched successfully: {}", user.getEmail());
            return UserResponse.from(user);
        } catch (UserServiceException e) {  // 👈 Catch specific exception
            throw e; // Re-throw directly without wrapping
        } catch (Exception e) {
            logger.error("Error fetching user by ID: {}", e.getMessage());
            throw new UserServiceException("Unable to fetch user details. Please check the user ID and try again.", e);
        }
    }


    @Override
    public List<UserResponse> getAllUsers(int page, int size) {
        try {
            logger.info("Fetching all users with pagination - page: {}, size: {}", page, size);
            List<UserResponse> users = userRepository.findAll()
                    .stream()
                    .map(UserResponse::from) // includes emailVerified
                    .toList();
            logger.info("Total users fetched: {}", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error fetching all users: {}", e.getMessage());
            throw new UserServiceException("Unable to fetch users. Please try again later.", e);
        }
    }

    @Override
    public void updateUserRole(String id, UpdateUserRoleRequest request) {
        try {
            logger.info("Updating role for user ID: {}", id);
            User user = userRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> {
                        logger.error("User not found with ID: {}", id);
                        return new UserServiceException("User not found with the provided ID.");
                    });
            user.setRole(request.getRole());
            userRepository.save(user);
            logger.info("User role updated successfully for ID: {}", id);
        } catch (UserServiceException e) {  // 👈 Catch specific exception
            throw e; // Re-throw directly
        } catch (Exception e) {
            logger.error("Error updating user role: {}", e.getMessage());
            throw new UserServiceException("Unable to update user role. Please try again later.", e);
        }
    }


    @Override
    public void deleteUser(String id) {
        try {
            logger.info("Deleting user with ID: {}", id);
            userRepository.deleteById(UUID.fromString(id));
            logger.info("User deleted successfully with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage());
            throw new UserServiceException("Unable to delete user. Please try again later.", e);
        }
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
                .roomsBooked(booking.getRoomsBooked())
                .build();
    }
}
