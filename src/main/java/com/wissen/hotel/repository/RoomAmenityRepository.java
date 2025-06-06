package com.wissen.hotel.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wissen.hotel.model.Room;
import com.wissen.hotel.model.RoomAmenity;

public interface RoomAmenityRepository extends JpaRepository<RoomAmenity, UUID> {

    Optional<RoomAmenity> findByRoom_RoomIdAndAmenity_AmenityId(UUID roomId, UUID amenityId);

    List<RoomAmenity> findByRoom_RoomId(UUID roomId);

}
