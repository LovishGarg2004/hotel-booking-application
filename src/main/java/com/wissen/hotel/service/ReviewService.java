package com.wissen.hotel.service;

import com.wissen.hotel.dto.request.CreateReviewRequest;
import com.wissen.hotel.dto.response.ReviewResponse;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    ReviewResponse createReview(CreateReviewRequest request);
    ReviewResponse getReviewById(UUID reviewId);
    void deleteReview(UUID reviewId);
    List<ReviewResponse> getReviewsByHotel(UUID hotelId);
}

