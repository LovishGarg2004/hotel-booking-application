// File: UserService.java
package com.wissen.hotel.services;

import com.wissen.hotel.dtos.*;
import java.util.List;

public interface UserService {
    UserResponse getCurrentUser();
    UserResponse updateCurrentUser(UpdateUserRequest request);
    List<BookingResponse> getCurrentUserBookings();
    UserResponse getUserById(String id);
    List<UserResponse> getAllUsers(int page, int size);
    void updateUserRole(String id, UpdateUserRoleRequest request);
    void deleteUser(String id);
}
