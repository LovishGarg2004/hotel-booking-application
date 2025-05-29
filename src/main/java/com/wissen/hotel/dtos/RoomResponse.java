package com.wissen.hotel.dtos;

import com.wissen.hotel.enums.RoomType;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {
    private UUID roomId;
    private UUID hotelId;
    private RoomType roomType;
    private int capacity;
    private BigDecimal basePrice;
    private int totalRooms;
    private List<AmenityResponse> amenities;
}
