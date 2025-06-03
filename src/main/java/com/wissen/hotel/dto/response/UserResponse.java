package com.wissen.hotel.dto.response;

import com.wissen.hotel.model.User;
import com.wissen.hotel.enums.UserRole;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private LocalDate dob;
    private UserRole role;
    private boolean emailVerified;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getUserId().toString())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .dob(user.getDob())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
