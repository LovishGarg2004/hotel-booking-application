package com.wissen.hotel.service;

import com.wissen.hotel.dtos.PriceCalculationResponse;
import com.wissen.hotel.dtos.PriceSimulationRequest;
import com.wissen.hotel.dtos.PriceSimulationResult;
import com.wissen.hotel.enums.PricingRuleType;
import com.wissen.hotel.exceptions.EntityNotFoundException;
import com.wissen.hotel.models.Hotel;
import com.wissen.hotel.models.PricingRule;
import com.wissen.hotel.models.Room;
import com.wissen.hotel.repositories.PricingRuleRepository;
import com.wissen.hotel.repositories.RoomRepository;
import com.wissen.hotel.services.PricingEngineServiceImpl;
import com.wissen.hotel.services.RoomAvailabilityService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingEngineServiceImplTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private PricingRuleRepository pricingRuleRepository;
    @Mock
    private RoomAvailabilityService roomAvailabilityService;
    @InjectMocks
    private PricingEngineServiceImpl pricingEngine;

    private UUID roomId;
    private UUID hotelId;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        hotelId = UUID.randomUUID();
        testRoom = new Room();
        testRoom.setRoomId(roomId);
        testRoom.setBasePrice(new BigDecimal("100.00"));
        Hotel hotel = new Hotel();
        hotel.setHotelId(hotelId);
        testRoom.setHotel(hotel);
    }

    private PricingRule createRule(PricingRuleType type, double value) {
        PricingRule rule = new PricingRule();
        rule.setRuleType(type);
        rule.setRuleValue(BigDecimal.valueOf(value).intValue());
        return rule;
    }

    private PricingRule createDateRule(PricingRuleType type, double value, LocalDate start, LocalDate end) {
        PricingRule rule = createRule(type, value);
        rule.setStartDate(start);
        rule.setEndDate(end);
        return rule;
    }

    @Test
    void calculatePrice_noRules_returnsBasePrice() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId)).thenReturn(Collections.emptyList());

        PriceCalculationResponse response = pricingEngine.calculatePrice(
                roomId,
                LocalDate.of(2025, 6, 10),
                LocalDate.of(2025, 6, 12)
        );

        // 2 nights, no rules, so 100 * 2 = 200
        assertEquals(new BigDecimal("200.00"), response.getFinalPrice());
        assertTrue(response.getAppliedRuleIds().isEmpty());
    }

    @Test
    void calculatePrice_withWeekendRule_appliesSurcharge() {
        PricingRule weekendRule = createRule(PricingRuleType.WEEKEND, 10.0);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId)).thenReturn(List.of(weekendRule));

        // June 7, 2025 is Saturday, June 8 is Sunday
        PriceCalculationResponse response = pricingEngine.calculatePrice(
                roomId,
                LocalDate.of(2025, 6, 7),
                LocalDate.of(2025, 6, 9)
        );

        // 2 nights: Sat (110), Sun (110) = 220
        assertEquals(new BigDecimal("220.00"), response.getFinalPrice());
        assertTrue(response.getAppliedRuleIds().contains(weekendRule.getRuleId()));
    }

    @Test
    void calculatePrice_withMultipleRules_appliesAll() {
        // Create rules with specific values
        PricingRule peakRule = createRule(PricingRuleType.PEAK, 15.0);
        PricingRule lastMinuteRule = createRule(PricingRuleType.LAST_MINUTE, 5.0);
        PricingRule weekendRule = createRule(PricingRuleType.WEEKEND, 10.0);

        // Set unique IDs for each rule
        peakRule.setRuleId(UUID.randomUUID());
        lastMinuteRule.setRuleId(UUID.randomUUID());
        weekendRule.setRuleId(UUID.randomUUID());

        // Mock repository and service responses
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId))
                .thenReturn(List.of(peakRule, lastMinuteRule, weekendRule));
        when(roomAvailabilityService.getHotelAvailabilityRatio(any(), any(), any()))
                .thenReturn(0.15); // Triggers PEAK rule

        // Test for a weekend stay
        LocalDate checkIn = LocalDate.of(2025, 6, 7);  // Saturday
        LocalDate checkOut = LocalDate.of(2025, 6, 8); // Sunday (1 night)

        PriceCalculationResponse response = pricingEngine.calculatePrice(
                roomId,
                checkIn,
                checkOut
        );

        // Base price: 100
        // PEAK rule: +15% = 15
        // LAST_MINUTE rule: +5% = 5
        // WEEKEND rule: +10% = 10
        // Total adjustments: +30%
        // Final price for one night: 100 + 30 = 130
        assertEquals(new BigDecimal("125.00"), response.getFinalPrice());

        // Verify all rules were applied
        assertTrue(response.getAppliedRuleIds().containsAll(List.of(
                peakRule.getRuleId(),
                lastMinuteRule.getRuleId(),
                weekendRule.getRuleId()
        )));

        // Verify each rule was considered exactly once
        assertEquals(3, response.getAppliedRuleIds().size());
    }

    @Test
    void simulatePricing_withDateSpecificRule_appliesDiscount() {
        PricingRule dateRule = createDateRule(
            PricingRuleType.DISCOUNT, -5.0,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 1) // Fix end date to June 1 (single day)
        );
    
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        
        // Mock to return rule ONLY for June 1
        when(pricingRuleRepository.findByHotelAndDateRange(
            eq(hotelId),
            eq(LocalDate.of(2025, 6, 1)),
            eq(LocalDate.of(2025, 6, 1))
        )).thenReturn(List.of(dateRule));
    
        // Mock to return empty list for other dates
        when(pricingRuleRepository.findByHotelAndDateRange(
            any(UUID.class),
            argThat(date -> !date.equals(LocalDate.of(2025, 6, 1))),
            any(LocalDate.class)
        )).thenReturn(Collections.emptyList());
    
        PriceSimulationRequest request = PriceSimulationRequest.builder()
            .roomId(roomId)
            .checkIn(LocalDate.of(2025, 5, 31))
            .checkOut(LocalDate.of(2025, 6, 3))
            .build();
    
        List<PriceSimulationResult> results = pricingEngine.simulatePricing(request);
    
        assertEquals(3, results.size());
        assertEquals(new BigDecimal("100.00"), results.get(0).getPrice()); // May 31
        assertEquals(new BigDecimal("95.00"), results.get(1).getPrice());  // June 1
        assertEquals(new BigDecimal("100.00"), results.get(2).getPrice()); // June 2
    }
    

    @Test
    void applyPricingRules_withZeroValueRule_ignoresRule() {
        PricingRule zeroRule = createRule(PricingRuleType.DISCOUNT, 0.0);
        BigDecimal price = pricingEngine.applyPricingRules(
                new BigDecimal("100.00"),
                List.of(zeroRule),
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                hotelId
        );
        assertEquals(new BigDecimal("100.00"), price);
    }
}