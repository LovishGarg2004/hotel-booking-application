package com.wissen.hotel.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AmenityResponse {
    private UUID amenityId;
    private String name;
    private String description;
    private String imageUrl;
}
