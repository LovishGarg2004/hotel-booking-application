package com.wissen.hotel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomAmenityResponse {
    private UUID id;           // RoomAmenity entity ID
    private UUID roomId;       // Room ID
    private UUID amenityId;    // Amenity ID
    private String amenityName;
    private String amenityDescription;
}
