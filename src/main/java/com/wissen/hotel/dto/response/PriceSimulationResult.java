package com.wissen.hotel.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class PriceSimulationResult {
    private LocalDate date;
    private BigDecimal price;
    private List<UUID> appliedRuleIds;
}
