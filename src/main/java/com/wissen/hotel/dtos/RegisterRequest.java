package com.wissen.hotel.dtos;

import com.wissen.hotel.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private UserRole role; // Enum: ADMIN, USER
}
