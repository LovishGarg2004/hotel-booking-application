package com.wissen.hotel.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrUpdateAmenityRequest {
    private String name;
    private String description;
}
