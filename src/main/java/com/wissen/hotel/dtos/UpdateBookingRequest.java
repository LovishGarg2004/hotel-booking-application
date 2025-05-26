package com.wissen.hotel.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateBookingRequest {
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int guests;
    private int roomsBooked; // number of rooms booked for one night
}
