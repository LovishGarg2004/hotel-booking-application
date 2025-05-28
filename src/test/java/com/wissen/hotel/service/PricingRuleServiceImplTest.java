package com.wissen.hotel.service;

import com.wissen.hotel.dtos.PricingRuleRequest;
import com.wissen.hotel.enums.PricingRuleType;
import com.wissen.hotel.exceptions.EntityNotFoundException;
import com.wissen.hotel.models.Hotel;
import com.wissen.hotel.models.PricingRule;
import com.wissen.hotel.repositories.HotelRepository;
import com.wissen.hotel.repositories.PricingRuleRepository;
import com.wissen.hotel.services.PricingRuleServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingRuleServiceImplTest {

    @Mock
    private PricingRuleRepository pricingRuleRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private PricingRuleServiceImpl pricingRuleService;

    private UUID hotelId;
    private UUID ruleId;
    private Hotel hotel;
    private PricingRuleRequest request;
    private PricingRule existingRule;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        ruleId = UUID.randomUUID();
        
        hotel = new Hotel();
        hotel.setHotelId(hotelId);
        
        request = new PricingRuleRequest();
        request.setHotelId(hotelId);
        request.setRuleType(PricingRuleType.WEEKEND);
        request.setRuleValue(15);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(5));

        existingRule = new PricingRule();
        existingRule.setRuleId(ruleId);
        existingRule.setHotel(hotel);
        existingRule.setRuleType(PricingRuleType.WEEKEND);
        existingRule.setRuleValue(20);
        existingRule.setStartDate(LocalDate.now().minusDays(10));
        existingRule.setEndDate(LocalDate.now().plusDays(10));
    }

    @Test
    void createRule_ShouldCreateRuleWhenNoConflicts() {
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId)).thenReturn(Collections.emptyList());
        when(pricingRuleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PricingRule result = pricingRuleService.createRule(request);

        assertNotNull(result);
        assertEquals(PricingRuleType.WEEKEND, result.getRuleType());
        assertEquals(15, result.getRuleValue());
        verify(pricingRuleRepository).save(any());
    }

    @Test
    void createRule_ShouldThrowWhenHotelNotFound() {
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());
        
        assertThrows(EntityNotFoundException.class, () -> 
            pricingRuleService.createRule(request));
    }

    @Test
    void createRule_ShouldThrowWhenDuplicateRuleTypeExists() {
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId)).thenReturn(List.of(existingRule));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            pricingRuleService.createRule(request));
        
        assertTrue(ex.getMessage().contains("WEEKEND rule already exists"));
    }

    @Test
    void createRule_ShouldThrowWhenOverlappingDatesForOtherTypes() {
        request.setRuleType(PricingRuleType.SEASONAL);
        PricingRule overlappingRule = new PricingRule();
        overlappingRule.setRuleType(PricingRuleType.SEASONAL);
        overlappingRule.setStartDate(LocalDate.now().plusDays(1));
        overlappingRule.setEndDate(LocalDate.now().plusDays(10));
        
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId)).thenReturn(List.of(overlappingRule));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            pricingRuleService.createRule(request));
        
        assertTrue(ex.getMessage().contains("SEASONAL rule with overlapping dates"));
    }

    @Test
    void createRule_ShouldSetNegativeValueForDiscount() {
        request.setRuleType(PricingRuleType.DISCOUNT);
        request.setRuleValue(20);
        
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId)).thenReturn(Collections.emptyList());
        when(pricingRuleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PricingRule result = pricingRuleService.createRule(request);
        
        assertEquals(-20, result.getRuleValue());
    }

    @Test
    void getAllRules_ShouldReturnAllRules() {
        when(pricingRuleRepository.findAll()).thenReturn(List.of(existingRule));
        
        List<PricingRule> result = pricingRuleService.getAllRules();
        
        assertEquals(1, result.size());
        assertEquals(existingRule, result.get(0));
    }

    @Test
    void getRuleById_ShouldReturnRuleWhenExists() {
        when(pricingRuleRepository.findById(ruleId)).thenReturn(Optional.of(existingRule));
        
        PricingRule result = pricingRuleService.getRuleById(ruleId);
        
        assertEquals(existingRule, result);
    }

    @Test
    void getRuleById_ShouldThrowWhenNotFound() {
        when(pricingRuleRepository.findById(ruleId)).thenReturn(Optional.empty());
        
        assertThrows(EntityNotFoundException.class, () -> 
            pricingRuleService.getRuleById(ruleId));
    }

    @Test
    void updateRule_ShouldUpdateRuleWhenNoConflicts() {
        PricingRuleRequest updateRequest = new PricingRuleRequest();
        updateRequest.setRuleType(PricingRuleType.WEEKEND);
        updateRequest.setRuleValue(25);
        updateRequest.setStartDate(LocalDate.now().plusDays(1));
        updateRequest.setEndDate(LocalDate.now().plusDays(6));

        when(pricingRuleRepository.findById(ruleId)).thenReturn(Optional.of(existingRule));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId)).thenReturn(Collections.emptyList());
        when(pricingRuleRepository.save(any())).thenReturn(existingRule);

        PricingRule result = pricingRuleService.updateRule(ruleId, updateRequest);
        
        assertEquals(25, result.getRuleValue());
        verify(pricingRuleRepository).save(any());
    }

    @Test
    void updateRule_ShouldThrowWhenDuplicateRuleTypeExists() {
        PricingRuleRequest updateRequest = new PricingRuleRequest();
        updateRequest.setRuleType(PricingRuleType.WEEKEND);
        PricingRule duplicateRule = new PricingRule();
        duplicateRule.setRuleId(UUID.randomUUID());
        duplicateRule.setRuleType(PricingRuleType.WEEKEND);

        when(pricingRuleRepository.findById(ruleId)).thenReturn(Optional.of(existingRule));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId)).thenReturn(List.of(duplicateRule));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            pricingRuleService.updateRule(ruleId, updateRequest));
        
        assertTrue(ex.getMessage().contains("WEEKEND rule already exists"));
    }

    @Test
    void updateRule_ShouldThrowWhenOverlappingDatesForOtherTypes() {
        PricingRuleRequest updateRequest = new PricingRuleRequest();
        updateRequest.setRuleType(PricingRuleType.SEASONAL);
        updateRequest.setStartDate(LocalDate.now());
        updateRequest.setEndDate(LocalDate.now().plusDays(5));
        
        PricingRule overlappingRule = new PricingRule();
        overlappingRule.setRuleId(UUID.randomUUID()); // Ensure ruleId is initialized
        overlappingRule.setRuleType(PricingRuleType.SEASONAL);
        overlappingRule.setStartDate(LocalDate.now().plusDays(1));
        overlappingRule.setEndDate(LocalDate.now().plusDays(10));

        when(pricingRuleRepository.findById(ruleId)).thenReturn(Optional.of(existingRule));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId)).thenReturn(List.of(overlappingRule));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            pricingRuleService.updateRule(ruleId, updateRequest));
        
        assertTrue(ex.getMessage().contains("SEASONAL rule with overlapping dates"));
    }

    @Test
    void deleteRule_ShouldCallRepositoryDelete() {
        doNothing().when(pricingRuleRepository).deleteById(ruleId);
        
        pricingRuleService.deleteRule(ruleId);
        
        verify(pricingRuleRepository).deleteById(ruleId);
    }

    @Test
    void getRulesForHotel_ShouldReturnHotelRules() {
        when(pricingRuleRepository.findByHotel_HotelId(hotelId)).thenReturn(List.of(existingRule));
        
        List<PricingRule> result = pricingRuleService.getRulesForHotel(hotelId);
        
        assertEquals(1, result.size());
        assertEquals(existingRule, result.get(0));
    }
}
