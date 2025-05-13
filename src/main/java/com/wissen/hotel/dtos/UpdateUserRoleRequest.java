package com.wissen.hotel.dtos;

import com.wissen.hotel.enums.UserRole;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UpdateUserRoleRequest {
    private UserRole role;
}
