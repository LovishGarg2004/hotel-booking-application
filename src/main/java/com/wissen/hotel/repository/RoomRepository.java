package com.wissen.hotel.repository;

import com.wissen.hotel.model.Room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findAllByHotel_HotelId(UUID hotelId);
    boolean existsByRoomIdAndHotelOwnerEmail(UUID roomId, String email);
    boolean existsByRoomIdAndHotel_Owner_Email(UUID roomId, String email);  // Ad
}