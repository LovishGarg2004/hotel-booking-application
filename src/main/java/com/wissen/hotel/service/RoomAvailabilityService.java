package com.wissen.hotel.service;

import com.wissen.hotel.dto.request.UpdateInventoryRequest;
import com.wissen.hotel.dto.request.BlockRoomRequest;

import java.time.LocalDate;
import java.util.UUID;

public interface RoomAvailabilityService {
    boolean isRoomAvailable(UUID roomId, LocalDate date);
    boolean isRoomAvailableForRange(UUID roomId, LocalDate start, LocalDate end);
    void updateInventory(UUID roomId, UpdateInventoryRequest request);
    void blockRoomDates(UUID roomId, BlockRoomRequest request);
    void unblockRoomDates(UUID roomId, BlockRoomRequest request);
    double getHotelAvailabilityRatio(UUID hotelId, LocalDate checkIn, LocalDate checkOut);
}
