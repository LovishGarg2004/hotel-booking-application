package com.wissen.hotel.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateBookingRequest {
    private UUID roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int guests;
    private int roomsBooked; // number of rooms booked for one night
}
