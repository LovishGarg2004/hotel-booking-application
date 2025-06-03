package com.wissen.hotel.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateReviewRequest {
    private UUID hotelId;
    private int rating;
    private String comment;
}
