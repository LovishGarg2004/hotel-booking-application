package com.wissen.hotel.dto.request;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInventoryRequest {
    private LocalDate date;
    private int roomsToBook;
}
