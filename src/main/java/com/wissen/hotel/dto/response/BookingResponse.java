package com.wissen.hotel.dto.response;

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
    private int roomsBooked; // number of rooms booked for one night
    private BigDecimal finalPrice;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
