package com.wissen.hotel.repositories;

import com.wissen.hotel.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByHotel_HotelId(UUID hotelId);
}
