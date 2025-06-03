package com.wissen.hotel.dto.request;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockRoomRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}
