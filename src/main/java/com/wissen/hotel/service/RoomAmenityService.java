package com.wissen.hotel.service;

import com.wissen.hotel.dto.response.AmenityResponse;
import com.wissen.hotel.dto.response.RoomAmenityResponse;
import java.util.List;
import java.util.UUID;

public interface RoomAmenityService {
    RoomAmenityResponse addAmenityToRoom(UUID roomId, UUID amenityId);
    void removeAmenityFromRoom(UUID roomId, UUID amenityId);
    List<AmenityResponse> getAmenitiesForRoom(UUID roomId);
}

