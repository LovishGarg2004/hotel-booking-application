package com.wissen.hotel.repositories;

import com.wissen.hotel.models.Review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
}