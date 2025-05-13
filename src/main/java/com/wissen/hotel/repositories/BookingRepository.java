package com.wissen.hotel.repositories;

import com.wissen.hotel.models.Booking;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

}