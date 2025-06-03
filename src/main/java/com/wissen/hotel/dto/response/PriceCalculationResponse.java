package com.wissen.hotel.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class PriceCalculationResponse {
    private UUID roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BigDecimal basePrice;
    private BigDecimal finalPrice;
    private List<UUID> appliedRuleIds;
}
