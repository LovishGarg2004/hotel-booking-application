package com.wissen.hotel.services;

import com.wissen.hotel.dtos.CreateReviewRequest;
import com.wissen.hotel.dtos.ReviewResponse;
import com.wissen.hotel.exceptions.ResourceNotFoundException;
import com.wissen.hotel.models.Hotel;
import com.wissen.hotel.models.Review;
import com.wissen.hotel.models.User;
import com.wissen.hotel.repositories.HotelRepository;
import com.wissen.hotel.repositories.ReviewRepository;
import com.wissen.hotel.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;

    @Override
    public ReviewResponse createReview(CreateReviewRequest request) {
        // UUID userId = AuthUtils.getCurrentUserId(); // Replace with proper authentication
        // User user = userRepository.findById(userId)
        //         .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Hotel hotel = hotelRepository.findById(request.getHotelId())
        //         .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        Review review = Review.builder()
                //.user(user)
                //.hotel(hotel)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        return mapToResponse(reviewRepository.save(review));
    }

    @Override
    public ReviewResponse getReviewById(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        return mapToResponse(review);
    }

    @Override
    public List<ReviewResponse> getReviewsByHotel(UUID hotelId) {
        List<Review> reviews = reviewRepository.findByHotel_HotelId(hotelId);
        return reviews.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteReview(UUID reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .userId(review.getUser().getUserId())
                .hotelId(review.getHotel().getHotelId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
