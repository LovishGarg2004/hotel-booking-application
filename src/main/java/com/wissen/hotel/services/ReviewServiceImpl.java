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
import com.wissen.hotel.utils.AuthUtil;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
        try {
            logger.info("Creating review for hotelId: {} by user: {}", request.getHotelId(), AuthUtil.getCurrentUser() != null ? AuthUtil.getCurrentUser().getUserId() : null);
            User user = AuthUtil.getCurrentUser(); // Replace with proper authentication

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
        } catch (Exception e) {
            throw new RuntimeException("Failed to create review. Please try again later.", e);
        }
    }

    @Override
    public ReviewResponse getReviewById(UUID reviewId) {
        try {
            logger.info("Fetching review by ID: {}", reviewId);
            Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
            logger.debug("Review found: {}", review.getReviewId());
            return mapToResponse(review);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch review. Please try again later.", e);
        }
    }

    @Override
    public List<ReviewResponse> getReviewsByHotel(UUID hotelId) {
        try {
            logger.info("Fetching reviews for hotelId: {}", hotelId);
            List<Review> reviews = reviewRepository.findByHotel_HotelId(hotelId);
            logger.debug("Found {} reviews for hotelId: {}", reviews.size(), hotelId);
            return reviews.stream().map(this::mapToResponse).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch reviews. Please try again later.", e);
        }
    }

    @Override
    public void deleteReview(UUID reviewId) {
        try {
            logger.info("Deleting review with ID: {}", reviewId);

            // Fetch review using existing method
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

            // Get authenticated user using AuthUtil
            User currentUser = AuthUtil.getCurrentUser();

            // Check if user is admin
            boolean isAdmin = currentUser.getRole().getAuthorities().stream()
                    .anyMatch(role -> "ROLE_ADMIN".equalsIgnoreCase(role.toString()));

            // Check permission: only review owner or admin can delete
            if (!review.getUser().getUserId().equals(currentUser.getUserId()) && !isAdmin) {
                throw new org.springframework.security.access.AccessDeniedException("You are not authorized to delete this review.");
            }

            reviewRepository.deleteById(reviewId);
            logger.debug("Review deleted: {}", reviewId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete review. Please try again later.", e);
        }
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
