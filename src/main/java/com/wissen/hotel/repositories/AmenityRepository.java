package com.wissen.hotel.repositories;

import com.wissen.hotel.models.Amenity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AmenityRepository extends JpaRepository<Amenity, UUID> {
    
}