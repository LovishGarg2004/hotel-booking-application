package com.wissen.hotel.dto.request;

import com.wissen.hotel.enums.RoomType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRoomRequest {
    private RoomType roomType;
    private int capacity;
    private BigDecimal basePrice;
    private int totalRooms;
}
