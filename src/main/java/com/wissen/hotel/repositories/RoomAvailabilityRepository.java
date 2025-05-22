package com.wissen.hotel.repositories;

import com.wissen.hotel.models.RoomAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, UUID> {

    RoomAvailability findByRoom_RoomIdAndDate(UUID roomId, LocalDate date);

 
}