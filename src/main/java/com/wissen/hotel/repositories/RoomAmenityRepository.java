package com.wissen.hotel.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wissen.hotel.models.Room;
import com.wissen.hotel.models.RoomAmenity;

public interface RoomAmenityRepository extends JpaRepository<RoomAmenity, UUID> {

    Optional<RoomAmenity> findByRoom_RoomIdAndAmenity_AmenityId(UUID roomId, UUID amenityId);

    List<RoomAmenity> findByRoom_RoomId(UUID roomId);

}
