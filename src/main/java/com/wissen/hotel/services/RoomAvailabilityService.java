package com.wissen.hotel.services;

import com.wissen.hotel.dtos.UpdateInventoryRequest;
import com.wissen.hotel.dtos.BlockRoomRequest;

import java.time.LocalDate;
import java.util.UUID;

public interface RoomAvailabilityService {
    boolean isRoomAvailable(UUID roomId, LocalDate date);
    boolean isRoomAvailableForRange(UUID roomId, LocalDate start, LocalDate end);
    void updateInventory(UUID roomId, UpdateInventoryRequest request);
    void blockRoomDates(UUID roomId, BlockRoomRequest request);
    void unblockRoomDates(UUID roomId, BlockRoomRequest request);
}
