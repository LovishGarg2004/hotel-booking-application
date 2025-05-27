package com.wissen.hotel.services;

import com.wissen.hotel.dtos.PricingRuleRequest;
import com.wissen.hotel.models.Hotel;
import com.wissen.hotel.models.PricingRule;
import com.wissen.hotel.repositories.HotelRepository;
import com.wissen.hotel.repositories.PricingRuleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingRuleServiceImpl implements PricingRuleService {
    private final PricingRuleRepository pricingRuleRepository;
    private final HotelRepository hotelRepository;

    @Override
    public PricingRule createRule(PricingRuleRequest request) {
        Hotel hotel = hotelRepository.findById(request.getHotelId())
            .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));
        int ruleValue = request.getRuleValue();
        if ("DISCOUNT".equalsIgnoreCase(request.getRuleType().toString())) {
            ruleValue = -Math.abs(ruleValue);
        }
        PricingRule rule = new PricingRule();
        rule.setHotel(hotel);
        rule.setRuleType(request.getRuleType());
        rule.setRuleValue(ruleValue);
        rule.setStartDate(request.getStartDate());
        rule.setEndDate(request.getEndDate());
        return pricingRuleRepository.save(rule);
    }

    @Override
    public List<PricingRule> getAllRules() {
        return pricingRuleRepository.findAll();
    }

    @Override
    public PricingRule getRuleById(UUID id) {
        return pricingRuleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("PricingRule not found"));
    }

    @Override
    public PricingRule updateRule(UUID id, PricingRuleRequest request) {
        PricingRule rule = getRuleById(id);
        rule.setRuleType(request.getRuleType());
        rule.setRuleValue(request.getRuleValue());
        rule.setStartDate(request.getStartDate());
        rule.setEndDate(request.getEndDate());
        return pricingRuleRepository.save(rule);
    }

    @Override
    public void deleteRule(UUID id) {
        pricingRuleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<PricingRule> getRulesForHotel(UUID hotelId) {
        return pricingRuleRepository.findByHotel_HotelId(hotelId);
    }
}
