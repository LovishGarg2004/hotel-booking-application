package com.wissen.hotel.dtos;

import com.wissen.hotel.enums.RoomType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {
    private RoomType roomType;
    private int capacity;
    private BigDecimal basePrice;
    private int totalRooms;
}
