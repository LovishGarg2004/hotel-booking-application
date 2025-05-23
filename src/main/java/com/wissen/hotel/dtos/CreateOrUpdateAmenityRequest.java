package com.wissen.hotel.dtos;

import lombok.Data;

@Data
public class CreateOrUpdateAmenityRequest {
    private String name;
    private String description;
    private String imageUrl;
}
