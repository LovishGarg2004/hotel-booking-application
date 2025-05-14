package com.wissen.hotel.enums;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum UserRole {
	ADMIN,
    CUSTOMER,
    HOTEL_OWNER
}
