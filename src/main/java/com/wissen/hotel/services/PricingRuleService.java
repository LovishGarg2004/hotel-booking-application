package com.wissen.hotel.services;

import com.wissen.hotel.dtos.PricingRuleRequest;
import com.wissen.hotel.models.PricingRule;
import java.util.List;
import java.util.UUID;

public interface PricingRuleService {
    PricingRule createRule(PricingRuleRequest request);
    List<PricingRule> getAllRules();
    PricingRule getRuleById(UUID id);
    PricingRule updateRule(UUID id, PricingRuleRequest request);
    void deleteRule(UUID id);
    List<PricingRule> getRulesForHotel(UUID hotelId);
}

