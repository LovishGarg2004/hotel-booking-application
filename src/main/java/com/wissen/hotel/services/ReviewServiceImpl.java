package com.wissen.hotel.services;

import com.wissen.hotel.dtos.CreateReviewRequest;
import com.wissen.hotel.dtos.ReviewResponse;
import com.wissen.hotel.exceptions.ResourceNotFoundException;
import com.wissen.hotel.models.Hotel;
import com.wissen.hotel.models.Review;
import com.wissen.hotel.models.User;
import com.wissen.hotel.repositories.HotelRepository;
import com.wissen.hotel.repositories.ReviewRepository;
import com.wissen.hotel.utils.AuthUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;

    @Override
    public ReviewResponse createReview(CreateReviewRequest request) {
        logger.info("Creating review for hotelId: {} by user: {}", request.getHotelId(), AuthUtil.getCurrentUser() != null ? AuthUtil.getCurrentUser().getUserId() : null);
        User user = AuthUtil.getCurrentUser();

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        Review review = Review.builder()
                .user(user)
                .hotel(hotel)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepository.save(review);
        logger.debug("Review created with ID: {}", saved.getReviewId());
        return mapToResponse(saved);
    }

    @Override
    public ReviewResponse getReviewById(UUID reviewId) {
        logger.info("Fetching review by ID: {}", reviewId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        logger.debug("Review found: {}", review.getReviewId());
        return mapToResponse(review);
    }

    @Override
    @Transactional
    public List<ReviewResponse> getReviewsByHotel(UUID hotelId) {
        logger.info("Fetching reviews for hotelId: {}", hotelId);
        List<Review> reviews = reviewRepository.findByHotel_HotelId(hotelId);
        logger.debug("Found {} reviews for hotelId: {}", reviews.size(), hotelId);
        return reviews.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteReview(UUID reviewId) {
        logger.info("Deleting review with ID: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        User currentUser = AuthUtil.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == com.wissen.hotel.enums.UserRole.ADMIN;

        if (!review.getUser().getUserId().equals(currentUser.getUserId()) && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to delete this review.");
        }

        reviewRepository.deleteById(reviewId);
        logger.debug("Review deleted: {}", reviewId);
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
