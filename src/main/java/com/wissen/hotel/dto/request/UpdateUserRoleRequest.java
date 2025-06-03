package com.wissen.hotel.dto.request;

import com.wissen.hotel.enums.UserRole;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UpdateUserRoleRequest {
    private UserRole role;
}
