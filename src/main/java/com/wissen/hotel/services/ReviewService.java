package com.wissen.hotel.services;

import com.wissen.hotel.dtos.CreateReviewRequest;
import com.wissen.hotel.dtos.ReviewResponse;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    ReviewResponse createReview(CreateReviewRequest request);
    ReviewResponse getReviewById(UUID reviewId);
    void deleteReview(UUID reviewId);
    List<ReviewResponse> getReviewsByHotel(UUID hotelId);
}

