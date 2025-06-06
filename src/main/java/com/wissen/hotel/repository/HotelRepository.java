package com.wissen.hotel.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wissen.hotel.model.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, UUID> {
    boolean existsByHotelIdAndOwnerEmail(UUID hotelId, String email);
    
    @Query("SELECT COUNT(h) > 0 FROM Hotel h WHERE h.hotelId = :id AND h.owner.email = :email")
    boolean existsByIdAndOwnerEmail(UUID id, String email);
    
}