package com.wissen.hotel.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class HotelResponse {
    private UUID hotelId;
    private String name;
    private String description;
    private String address;
    private String city;
    private String state;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean isApproved;
    private LocalDateTime createdAt;
    private UUID ownerId; // only exposing owner ID
    private int roomsRequired; // Number of rooms required for the search request
    private BigDecimal finalPrice; // Final price for the stay (all rooms, all days)
}