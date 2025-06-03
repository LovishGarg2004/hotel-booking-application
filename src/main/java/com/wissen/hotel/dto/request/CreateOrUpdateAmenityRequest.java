package com.wissen.hotel.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrUpdateAmenityRequest {
    private String name;
    private String description;
}
