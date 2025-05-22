package com.wissen.hotel.dtos;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BlockRoomRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}
