package com.wissen.hotel.services;

import com.wissen.hotel.dtos.AmenityResponse;
import com.wissen.hotel.dtos.RoomAmenityResponse;
import java.util.List;
import java.util.UUID;

public interface RoomAmenityService {
    RoomAmenityResponse addAmenityToRoom(UUID roomId, UUID amenityId);
    void removeAmenityFromRoom(UUID roomId, UUID amenityId);
    List<AmenityResponse> getAmenitiesForRoom(UUID roomId);
}

