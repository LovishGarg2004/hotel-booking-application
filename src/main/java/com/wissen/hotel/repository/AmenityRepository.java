package com.wissen.hotel.repository;

import com.wissen.hotel.model.Amenity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AmenityRepository extends JpaRepository<Amenity, UUID> {
    Amenity findByName(String name) ;
}