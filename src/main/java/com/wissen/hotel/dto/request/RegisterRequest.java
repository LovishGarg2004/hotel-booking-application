package com.wissen.hotel.dto.request;

import java.time.LocalDate;

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
    private String phone;
    private LocalDate dob; // Date of Birth in a suitable format (e.g., yyyy-MM-dd)
}
