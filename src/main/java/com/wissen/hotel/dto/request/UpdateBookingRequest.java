package com.wissen.hotel.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class UpdateBookingRequest {
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int guests;
    private int roomsBooked; // number of rooms booked for one night
}
