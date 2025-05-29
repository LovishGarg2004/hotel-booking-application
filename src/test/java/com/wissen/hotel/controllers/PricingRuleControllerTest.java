package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.PricingRuleRequest;
import com.wissen.hotel.models.PricingRule;
import com.wissen.hotel.services.PricingRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PricingRuleControllerTest {

    private PricingRuleService pricingRuleService;
    private PricingRuleController controller;

    @BeforeEach
    void setUp() {
        pricingRuleService = mock(PricingRuleService.class);
        controller = new PricingRuleController(pricingRuleService);
    }

    @Test
    void createRule_shouldReturnCreatedRule() {
        PricingRuleRequest request = new PricingRuleRequest();
        PricingRule rule = new PricingRule();
        when(pricingRuleService.createRule(request)).thenReturn(rule);

        ResponseEntity<PricingRule> result = controller.createRule(request);

        assertEquals(200, result.getStatusCodeValue());
        assertSame(rule, result.getBody());
        verify(pricingRuleService, times(1)).createRule(request);
    }

    @Test
    void getAllRules_shouldReturnListOfRules() {
        List<PricingRule> rules = Arrays.asList(new PricingRule(), new PricingRule());
        when(pricingRuleService.getAllRules()).thenReturn(rules);

        ResponseEntity<List<PricingRule>> result = controller.getAllRules();

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(rules, result.getBody());
        verify(pricingRuleService, times(1)).getAllRules();
    }

    @Test
    void getRuleById_shouldReturnRule() {
        UUID id = UUID.randomUUID();
        PricingRule rule = new PricingRule();
        when(pricingRuleService.getRuleById(id)).thenReturn(rule);

        ResponseEntity<PricingRule> result = controller.getRuleById(id);

        assertEquals(200, result.getStatusCodeValue());
        assertSame(rule, result.getBody());
        verify(pricingRuleService, times(1)).getRuleById(id);
    }

    @Test
    void updateRule_shouldReturnUpdatedRule() {
        UUID id = UUID.randomUUID();
        PricingRuleRequest request = new PricingRuleRequest();
        PricingRule updatedRule = new PricingRule();
        when(pricingRuleService.updateRule(id, request)).thenReturn(updatedRule);

        ResponseEntity<PricingRule> result = controller.updateRule(id, request);

        assertEquals(200, result.getStatusCodeValue());
        assertSame(updatedRule, result.getBody());
        verify(pricingRuleService, times(1)).updateRule(id, request);
    }

    @Test
    void deleteRule_shouldReturnNoContent() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> result = controller.deleteRule(id);

        assertEquals(204, result.getStatusCodeValue());
        verify(pricingRuleService, times(1)).deleteRule(id);
    }

    @Test
    void getRulesForHotel_shouldReturnListOfRules() {
        UUID hotelId = UUID.randomUUID();
        List<PricingRule> rules = Arrays.asList(new PricingRule(), new PricingRule());
        when(pricingRuleService.getRulesForHotel(hotelId)).thenReturn(rules);

        ResponseEntity<List<PricingRule>> result = controller.getRulesForHotel(hotelId);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(rules, result.getBody());
        verify(pricingRuleService, times(1)).getRulesForHotel(hotelId);
    }
}