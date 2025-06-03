package com.wissen.hotel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class AmenityResponse {
    private UUID amenityId;
    private String name;
    private String description;
}
