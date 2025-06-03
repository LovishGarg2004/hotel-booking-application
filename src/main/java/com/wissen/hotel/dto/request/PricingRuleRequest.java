package com.wissen.hotel.dto.request;

import com.wissen.hotel.enums.PricingRuleType;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class PricingRuleRequest {
    private UUID hotelId;
    private PricingRuleType ruleType;
    private Integer ruleValue; // Percentage increase
    private LocalDate startDate;
    private LocalDate endDate;
}
