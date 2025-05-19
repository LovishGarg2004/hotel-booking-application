package com.wissen.hotel.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID reviewId;
    private UUID userId;
    private UUID hotelId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
