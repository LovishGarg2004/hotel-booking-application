package com.wissen.hotel.service;

import com.wissen.hotel.dto.request.UpdateUserRequest;
import com.wissen.hotel.dto.request.UpdateUserRoleRequest;
import com.wissen.hotel.dto.response.UserResponse;
import com.wissen.hotel.enums.UserRole;
import com.wissen.hotel.dto.response.BookingResponse;
import com.wissen.hotel.model.Booking;
import com.wissen.hotel.model.Room;
import com.wissen.hotel.model.User;
import com.wissen.hotel.repository.BookingRepository;
import com.wissen.hotel.repository.UserRepository;
import com.wissen.hotel.service.impl.UserServiceImpl;
import com.wissen.hotel.util.AuthUtil;
import com.wissen.hotel.exception.UserServiceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
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
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);

            UpdateUserRequest request = new UpdateUserRequest();
            request.setName("Updated Name");
            request.setPhone("9999999999");
            request.setDob(LocalDate.of(1998, 5, 10));

            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserResponse response = userService.updateCurrentUser(request);

            assertNotNull(response);
            assertEquals("Updated Name", response.getName());
            assertEquals("9999999999", response.getPhone());
            assertEquals(LocalDate.of(1998, 5, 10), response.getDob());

            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    @Test
    void updateCurrentUser_ShouldThrowWhenSavingFails() {
        try (var mockedAuth = mockStatic(AuthUtil.class)) {
            mockedAuth.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
            when(userRepository.save(any())).thenThrow(new DataAccessException("DB error") {});

            UserServiceException ex = assertThrows(UserServiceException.class,
                () -> userService.updateCurrentUser(new UpdateUserRequest()));

            assertTrue(ex.getMessage().contains("Unable to update user profile"));
            assertTrue(ex.getCause().getMessage().contains("DB error"));
        }
    }

    @Test
    void testGetCurrentUserBookings_Success() {
        try (MockedStatic<AuthUtil> authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(AuthUtil::getCurrentUser).thenReturn(mockUser);

            Room mockRoom1 = new Room();
            mockRoom1.setRoomId(UUID.randomUUID());

            Room mockRoom2 = new Room();
            mockRoom2.setRoomId(UUID.randomUUID());

            Booking booking1 = new Booking();
            booking1.setBookingId(UUID.randomUUID());
            booking1.setUser(mockUser);
            booking1.setRoom(mockRoom1);
            booking1.setGuests(2);

            Booking booking2 = new Booking();
            booking2.setBookingId(UUID.randomUUID());
            booking2.setUser(mockUser);
            booking2.setRoom(mockRoom2);
            booking2.setGuests(4);

            when(bookingRepository.findAllByUser_UserId(mockUser.getUserId()))
                    .thenReturn(List.of(booking1, booking2));

            List<BookingResponse> bookings = userService.getCurrentUserBookings();

            assertNotNull(bookings);
            assertEquals(2, bookings.size());
            assertNotNull(bookings.get(0).getRoomId());
            verify(bookingRepository, times(1)).findAllByUser_UserId(mockUser.getUserId());
        }
    }

    @Test
    void getCurrentUserBookings_ShouldThrowWhenBookingsFetchFails() {
        try (var mockedAuth = mockStatic(AuthUtil.class)) {
            mockedAuth.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
            when(bookingRepository.findAllByUser_UserId(any()))
                .thenThrow(new RuntimeException("DB error"));

            UserServiceException ex = assertThrows(UserServiceException.class,
                () -> userService.getCurrentUserBookings());

            assertTrue(ex.getMessage().contains("Unable to fetch your bookings"));
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
        assertEquals(userId.toString(), response.getId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
    }

    @Test
    void testGetUserById_NotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            userService.getUserById(userId.toString());
        });

        assertEquals("User not found with the provided ID.", exception.getMessage());
    }

    @Test
    void getUserById_ShouldThrowWhenUserNotFound() {
        when(userRepository.findById(any()))
            .thenReturn(Optional.empty());

        UserServiceException ex = assertThrows(UserServiceException.class,
            () -> userService.getUserById(UUID.randomUUID().toString()));

        assertTrue(ex.getMessage().contains("User not found"));
        assertNull(ex.getCause());
    }

    @Test
    void getUserById_ShouldThrowOnInvalidUUID() {
        UserServiceException ex = assertThrows(UserServiceException.class,
            () -> userService.getUserById("invalid-id"));

        assertTrue(ex.getMessage().contains("Unable to fetch user details"));
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
    void getAllUsers_ShouldThrowWhenRepositoryFails() {
        when(userRepository.findAll()).thenThrow(new DataAccessException("DB error") {});

        UserServiceException ex = assertThrows(UserServiceException.class,
            () -> userService.getAllUsers(0, 10));

        assertTrue(ex.getMessage().contains("Unable to fetch users"));
    }

    @Test
    void testUpdateUserRole_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);
        user.setEmail("user@example.com");
        user.setRole(UserRole.CUSTOMER);

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

        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            userService.updateUserRole(userId.toString(), roleRequest);
        });

        assertEquals("User not found with the provided ID.", exception.getMessage());
    }

    @Test
    void updateUserRole_ShouldThrowWhenUserNotFound() {
        when(userRepository.findById(any()))
            .thenReturn(Optional.empty());

        UserServiceException ex = assertThrows(UserServiceException.class,
            () -> userService.updateUserRole(UUID.randomUUID().toString(), new UpdateUserRoleRequest()));

        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void updateUserRole_ShouldThrowWhenSavingFails() {
        when(userRepository.findById(any())).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any())).thenThrow(new DataAccessException("DB error") {});

        UserServiceException ex = assertThrows(UserServiceException.class,
            () -> userService.updateUserRole(UUID.randomUUID().toString(), new UpdateUserRoleRequest()));

        assertTrue(ex.getMessage().contains("Unable to update user role"));
    }

    // deleteUser exceptions
    @Test
    void deleteUser_ShouldThrowWhenDeletionFails() {
        doThrow(new DataAccessException("DB error") {})
            .when(userRepository).deleteById(any());

        UserServiceException ex = assertThrows(UserServiceException.class,
            () -> userService.deleteUser(UUID.randomUUID().toString()));

        assertTrue(ex.getMessage().contains("Unable to delete user"));
    }

    @Test
    void deleteUser_ShouldThrowOnInvalidUUID() {
        UserServiceException ex = assertThrows(UserServiceException.class,
            () -> userService.deleteUser("invalid-id"));

        assertTrue(ex.getMessage().contains("Unable to delete user"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void testDeleteUser_Success() {
        UUID userId = UUID.randomUUID();

        userService.deleteUser(userId.toString());

        verify(userRepository).deleteById(userId);
    }

    @Test
    void getCurrentUser_ShouldReturnUserResponse_WhenUserIsPresent() {
        User mockUser = new User();
        mockUser.setUserId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");
        mockUser.setEmailVerified(true);

        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getCurrentUser).thenReturn(mockUser);

            UserResponse response = userService.getCurrentUser();

            assertNotNull(response);
            assertEquals("test@example.com", response.getEmail());
            assertTrue(response.isEmailVerified());
        }
    }

    @Test
    void getCurrentUser_ShouldThrowUserServiceException_WhenExceptionOccurs() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getCurrentUser).thenThrow(new RuntimeException("Session expired"));

            UserServiceException ex = assertThrows(UserServiceException.class, () -> userService.getCurrentUser());
            assertTrue(ex.getMessage().contains("Unable to fetch current user"));
            assertTrue(ex.getCause() instanceof RuntimeException);
        }
    }
}

