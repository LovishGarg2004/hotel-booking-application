package com.wissen.hotel.service;

import com.wissen.hotel.dto.request.CreateReviewRequest;
import com.wissen.hotel.dto.response.ReviewResponse;
import com.wissen.hotel.enums.UserRole;
import com.wissen.hotel.exception.ResourceNotFoundException;
import com.wissen.hotel.model.*;
import com.wissen.hotel.repository.HotelRepository;
import com.wissen.hotel.repository.ReviewRepository;
import com.wissen.hotel.service.impl.ReviewServiceImpl;
import com.wissen.hotel.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private UUID userId;
    private UUID hotelId;
    private UUID reviewId;
    private User user;
    private Hotel hotel;
    private Review review;

    @BeforeEach
void setUp() {
    userId = UUID.randomUUID();
    hotelId = UUID.randomUUID();
    reviewId = UUID.randomUUID();

    user = User.builder()
            .userId(userId)
            .role(UserRole.CUSTOMER)  // Direct enum assignment
            .build();

    hotel = Hotel.builder()
            .hotelId(hotelId)
            .build();

    review = Review.builder()
            .reviewId(reviewId)
            .user(user)
            .hotel(hotel)
            .rating(4)
            .comment("Great experience")
            .createdAt(LocalDateTime.now())
            .build();
}


    @Test
    void createReview_ShouldCreateNewReview() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setHotelId(hotelId);
        request.setRating(5);
        request.setComment("Excellent service");

        try (MockedStatic<AuthUtil> utilities = Mockito.mockStatic(AuthUtil.class)) {
            utilities.when(AuthUtil::getCurrentUser).thenReturn(user);
            
            when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
            when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
                Review r = invocation.getArgument(0);
                r.setReviewId(UUID.randomUUID());
                return r;
            });

            ReviewResponse response = reviewService.createReview(request);

            assertNotNull(response);
            assertEquals(hotelId, response.getHotelId());
            assertEquals(userId, response.getUserId());
            assertEquals(5, response.getRating());
            assertEquals("Excellent service", response.getComment());
            
            verify(hotelRepository).findById(hotelId);
            verify(reviewRepository).save(any(Review.class));
        }
    }

    @Test
    void createReview_ShouldThrowWhenHotelNotFound() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setHotelId(hotelId);

        try (MockedStatic<AuthUtil> utilities = Mockito.mockStatic(AuthUtil.class)) {
            utilities.when(AuthUtil::getCurrentUser).thenReturn(user);
            when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> reviewService.createReview(request));
            verify(reviewRepository, never()).save(any());
        }
    }

    @Test
    void getReviewById_ShouldReturnReview() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        ReviewResponse response = reviewService.getReviewById(reviewId);

        assertNotNull(response);
        assertEquals(reviewId, response.getReviewId());
        assertEquals(userId, response.getUserId());
        assertEquals(4, response.getRating());
    }

    @Test
    void getReviewById_ShouldThrowWhenNotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewById(reviewId));
    }

    @Test
    void getReviewsByHotel_ShouldReturnReviewList() {
        List<Review> reviews = Collections.singletonList(review);
        when(reviewRepository.findByHotel_HotelId(hotelId)).thenReturn(reviews);

        List<ReviewResponse> responses = reviewService.getReviewsByHotel(hotelId);

        assertEquals(1, responses.size());
        assertEquals(reviewId, responses.get(0).getReviewId());
    }

    @Test
    void getReviewsByHotel_ShouldReturnEmptyList() {
        when(reviewRepository.findByHotel_HotelId(hotelId)).thenReturn(Collections.emptyList());

        List<ReviewResponse> responses = reviewService.getReviewsByHotel(hotelId);

        assertTrue(responses.isEmpty());
    }

    @Test
    void deleteReview_ShouldAllowOwner() {
        try (MockedStatic<AuthUtil> utilities = Mockito.mockStatic(AuthUtil.class)) {
            utilities.when(AuthUtil::getCurrentUser).thenReturn(user);
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

            reviewService.deleteReview(reviewId);

            verify(reviewRepository).deleteById(reviewId);
        }
    }

    @Test
    void deleteReview_ShouldAllowAdmin() {
        User adminUser = User.builder()
                .userId(UUID.randomUUID())
                .role(UserRole.ADMIN)  // Direct enum assignment
                .build();

        try (MockedStatic<AuthUtil> utilities = Mockito.mockStatic(AuthUtil.class)) {
            utilities.when(AuthUtil::getCurrentUser).thenReturn(adminUser);
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

            reviewService.deleteReview(reviewId);

            verify(reviewRepository).deleteById(reviewId);
        }
    }

    @Test
    void deleteReview_ShouldThrowWhenUnauthorized() {
        User otherUser = User.builder()
                .userId(UUID.randomUUID())
                .role(UserRole.CUSTOMER)  // Direct enum assignment
                .build();

        try (MockedStatic<AuthUtil> utilities = Mockito.mockStatic(AuthUtil.class)) {
            utilities.when(AuthUtil::getCurrentUser).thenReturn(otherUser);
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

            assertThrows(AccessDeniedException.class, () -> reviewService.deleteReview(reviewId));
            verify(reviewRepository, never()).deleteById(any());
        }
    }

    @Test
    void deleteReview_ShouldThrowWhenReviewNotFound() {
        try (MockedStatic<AuthUtil> utilities = Mockito.mockStatic(AuthUtil.class)) {
            utilities.when(AuthUtil::getCurrentUser).thenReturn(user);
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> reviewService.deleteReview(reviewId));
        }
    }

    @Test
    void mapToResponse_ShouldContainAllFields() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review)); // Mocking the repository

        ReviewResponse response = reviewService.getReviewById(reviewId);

        assertNotNull(response.getCreatedAt());
        assertEquals("Great experience", response.getComment());
        assertEquals(hotelId, response.getHotelId());
    }
}
