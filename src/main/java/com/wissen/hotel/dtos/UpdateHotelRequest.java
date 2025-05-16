package com.wissen.hotel.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateHotelRequest {
    private String name;
    private String description;
    private String address;
    private String city;
    private String state;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
}