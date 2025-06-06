package com.wissen.hotel.service;

import com.wissen.hotel.dto.request.*;
import com.wissen.hotel.dto.response.*;
import com.wissen.hotel.enums.RoomType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RoomService {
    RoomResponse createRoom(UUID hotelId, CreateRoomRequest request);
    RoomResponse getRoomById(UUID roomId);
    RoomResponse updateRoom(UUID roomId, UpdateRoomRequest request);
    void deleteRoom(UUID roomId);
    RoomResponse updateRoomAmenities(UUID roomId, List<String> amenities);
    List<String> getAllRoomTypes();
    boolean isRoomAvailable(UUID roomId, LocalDate checkIn, LocalDate checkOut);
}

