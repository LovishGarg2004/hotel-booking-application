package com.wissen.hotel.service;

import com.wissen.hotel.dtos.UpdateUserRequest;
import com.wissen.hotel.dtos.UpdateUserRoleRequest;
import com.wissen.hotel.dtos.UserResponse;
import com.wissen.hotel.enums.UserRole;
import com.wissen.hotel.dtos.BookingResponse;
import com.wissen.hotel.models.Booking;
import com.wissen.hotel.models.Room;
import com.wissen.hotel.models.User;
import com.wissen.hotel.repositories.BookingRepository;
import com.wissen.hotel.repositories.UserRepository;
import com.wissen.hotel.services.UserServiceImpl;
import com.wissen.hotel.utils.AuthUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(UUID.randomUUID());
        mockUser.setName("John Doe");
        mockUser.setEmail("john@example.com");
        mockUser.setPhone("1234567890");
        mockUser.setDob(LocalDate.of(2000, 1, 1));
    }

    @Test
    void testUpdateCurrentUser_Success() {
        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            // Mock AuthUtil
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);

            // Prepare request
            UpdateUserRequest request = new UpdateUserRequest();
            request.setName("Updated Name");
            request.setPhone("9999999999");
            request.setDob(LocalDate.of(1998, 5, 10));

            // Mock save behavior
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Call method
            UserResponse response = userService.updateCurrentUser(request);

            // Assertions
            assertNotNull(response);
            assertEquals("Updated Name", response.getName());
            assertEquals("9999999999", response.getPhone());
            assertEquals(LocalDate.of(1998, 5, 10), response.getDob());

            verify(userRepository, times(1)).save(mockUser);
        }
    }

    @Test
    void testGetCurrentUserBookings_Success() {
        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);

            // Mock Room
            Room mockRoom1 = new Room();
            mockRoom1.setRoomId(UUID.randomUUID());

            Room mockRoom2 = new Room();
            mockRoom2.setRoomId(UUID.randomUUID());

            // Mock Bookings
            Booking booking1 = new Booking();
            booking1.setBookingId(UUID.randomUUID());
            booking1.setUser(mockUser);
            booking1.setRoom(mockRoom1);  // ✅ Set room
            booking1.setGuests(2);

            Booking booking2 = new Booking();
            booking2.setBookingId(UUID.randomUUID());
            booking2.setUser(mockUser);
            booking2.setRoom(mockRoom2);  // ✅ Set room
            booking2.setGuests(4);

            when(bookingRepository.findAllByUser_UserId(mockUser.getUserId()))
                    .thenReturn(List.of(booking1, booking2));

            List<BookingResponse> bookings = userService.getCurrentUserBookings();

            assertNotNull(bookings);
            assertEquals(2, bookings.size());
            assertNotNull(bookings.get(0).getRoomId());  // Optional extra validation
            verify(bookingRepository, times(1)).findAllByUser_UserId(mockUser.getUserId());
        }
    }

    @Test
    void testGetUserById_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);
        user.setEmail("user@example.com");
        user.setName("Test User");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(userId.toString());

        assertNotNull(response);
        assertEquals(userId.toString(), response.getId()); // FIXED
        assertEquals("user@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
    }


    @Test
    void testGetUserById_NotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserById(userId.toString());
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User();
        user1.setUserId(UUID.randomUUID());
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setUserId(UUID.randomUUID());
        user2.setEmail("user2@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponse> result = userService.getAllUsers(0, 10);

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void testUpdateUserRole_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);
        user.setEmail("user@example.com");
        user.setRole(UserRole.CUSTOMER); // Initial role

        UpdateUserRoleRequest roleRequest = new UpdateUserRoleRequest();
        roleRequest.setRole(UserRole.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateUserRole(userId.toString(), roleRequest);

        assertEquals(UserRole.ADMIN, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateUserRole_NotFound() {
        UUID userId = UUID.randomUUID();
        UpdateUserRoleRequest roleRequest = new UpdateUserRoleRequest();
        roleRequest.setRole(UserRole.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUserRole(userId.toString(), roleRequest);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testDeleteUser_Success() {
        UUID userId = UUID.randomUUID();

        userService.deleteUser(userId.toString());

        verify(userRepository).deleteById(userId);
    }



}
