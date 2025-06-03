// File: UserService.java
package com.wissen.hotel.service;

import com.wissen.hotel.dto.request.*;
import com.wissen.hotel.dto.response.*;

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
