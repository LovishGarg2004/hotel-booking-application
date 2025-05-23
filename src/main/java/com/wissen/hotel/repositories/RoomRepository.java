package com.wissen.hotel.repositories;

import com.wissen.hotel.models.Room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    boolean existsByRoomIdAndHotelOwnerEmail(UUID roomId, String email);
    boolean existsByRoomIdAndHotel_Owner_Email(UUID roomId, String email);  // Ad
}