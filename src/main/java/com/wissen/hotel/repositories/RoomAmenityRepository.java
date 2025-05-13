package com.wissen.hotel.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wissen.hotel.models.RoomAmenity;

public interface RoomAmenityRepository extends JpaRepository<RoomAmenity, UUID> {

}
