package com.wissen.hotel.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateReviewRequest {
    private UUID hotelId;
    private int rating;
    private String comment;
}
