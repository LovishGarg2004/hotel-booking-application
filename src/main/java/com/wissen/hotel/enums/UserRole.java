package com.wissen.hotel.enums;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Collections;
import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum UserRole {
    ADMIN,
    CUSTOMER,
    HOTEL_OWNER;

    public List<String> getAuthorities() {
        switch (this) {
            case ADMIN:
                return List.of("ROLE_ADMIN", "ROLE_USER");
            case CUSTOMER:
                return List.of("ROLE_CUSTOMER");
            case HOTEL_OWNER:
                return List.of("ROLE_HOTEL_OWNER", "ROLE_CUSTOMER");
            default:
                return Collections.emptyList();
        }
    }
}
