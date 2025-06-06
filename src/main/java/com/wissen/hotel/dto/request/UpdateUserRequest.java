package com.wissen.hotel.dto.request;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UpdateUserRequest {
    private String name;
    private String phone;
    private LocalDate dob;
}
