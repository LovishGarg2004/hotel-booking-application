package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.PricingRuleRequest;
import com.wissen.hotel.models.PricingRule;
import com.wissen.hotel.services.PricingRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pricing/rules")
@RequiredArgsConstructor
public class PricingRuleController {
    private final PricingRuleService pricingRuleService;

    @PostMapping
    public ResponseEntity<PricingRule> createRule(@RequestBody PricingRuleRequest request) {
        return ResponseEntity.ok(pricingRuleService.createRule(request));
    }

    @GetMapping
    public ResponseEntity<List<PricingRule>> getAllRules() {
        return ResponseEntity.ok(pricingRuleService.getAllRules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PricingRule> getRuleById(@PathVariable UUID id) {
        return ResponseEntity.ok(pricingRuleService.getRuleById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PricingRule> updateRule(@PathVariable UUID id, @RequestBody PricingRuleRequest request) {
        return ResponseEntity.ok(pricingRuleService.updateRule(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        pricingRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<List<PricingRule>> getRulesForHotel(@PathVariable UUID hotelId) {
        return ResponseEntity.ok(pricingRuleService.getRulesForHotel(hotelId));
    }
}

