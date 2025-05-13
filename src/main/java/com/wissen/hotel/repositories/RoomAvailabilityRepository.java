package com.wissen.hotel.repositories;

import com.wissen.hotel.models.RoomAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, UUID> {

    // Find availability by room ID
    List<RoomAvailability> findByRoomId(UUID roomId);

    // Find availability for a specific date
    List<RoomAvailability> findByDate(LocalDate date);

    // Find availability for a specific room and date
    RoomAvailability findByRoomIdAndDate(UUID roomId, LocalDate date);
}