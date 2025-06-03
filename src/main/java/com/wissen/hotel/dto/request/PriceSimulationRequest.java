package com.wissen.hotel.dto.request;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceSimulationRequest {
    private UUID hotelId;
    private UUID roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int guests;
}
