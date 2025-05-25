package com.wissen.hotel.dtos;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateInventoryRequest {
    private LocalDate date;
    private int roomsToBook;
}
