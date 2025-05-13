package com.wissen.hotel.repositories;

import com.wissen.hotel.models.Hotel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HotelRepository extends JpaRepository<Hotel, UUID> {

}