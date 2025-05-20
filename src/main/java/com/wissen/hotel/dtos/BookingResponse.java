package com.wissen.hotel.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.wissen.hotel.enums.BookingStatus;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class BookingResponse {
    private UUID bookingId;
    private UUID userId;
    private UUID roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int guests;
    private BigDecimal finalPrice;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
