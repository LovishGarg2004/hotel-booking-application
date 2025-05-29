package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.CreateReviewRequest;
import com.wissen.hotel.dtos.ReviewResponse;
import com.wissen.hotel.services.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewControllerTest {

    private ReviewService reviewService;
    private ReviewController controller;

    @BeforeEach
    void setUp() {
        reviewService = mock(ReviewService.class);
        controller = new ReviewController(reviewService);
    }

    @Test
    void createReview_shouldReturnCreatedReview() {
        CreateReviewRequest request = new CreateReviewRequest();
        // Replace with appropriate constructor or builder as per ReviewResponse definition
        ReviewResponse response = mock(ReviewResponse.class);
        when(reviewService.createReview(request)).thenReturn(response);

        ResponseEntity<ReviewResponse> result = controller.createReview(request);

        assertEquals(201, result.getStatusCode().value());
        assertSame(response, result.getBody());
        verify(reviewService, times(1)).createReview(request);
    }

    @Test
    void getReviewById_shouldReturnReview() {
        UUID reviewId = UUID.randomUUID();
        ReviewResponse response = mock(ReviewResponse.class);
        when(reviewService.getReviewById(reviewId)).thenReturn(response);

        ResponseEntity<ReviewResponse> result = controller.getReviewById(reviewId);

        assertEquals(200, result.getStatusCode().value());
        assertSame(response, result.getBody());
        verify(reviewService, times(1)).getReviewById(reviewId);
    }

    @Test
    void deleteReview_shouldReturnNoContent() {
        UUID reviewId = UUID.randomUUID();

        ResponseEntity<Void> result = controller.deleteReview(reviewId);

        assertEquals(204, result.getStatusCode().value());
        verify(reviewService, times(1)).deleteReview(reviewId);
    }
}