package com.wissen.hotel.dtos;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class BookingResponse {
    private String bookingId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int guests;
    private double finalPrice;
    private String status;
}
