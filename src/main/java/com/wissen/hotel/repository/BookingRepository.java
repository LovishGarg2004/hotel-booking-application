package com.wissen.hotel.repository;

import com.wissen.hotel.model.Booking;
import com.wissen.hotel.model.Hotel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByRoom_RoomId(UUID roomId);
    List<Booking> findByRoom_Hotel_HotelId(UUID hotelId);
    List<Booking> findAllByUser_UserId(UUID userId);
}