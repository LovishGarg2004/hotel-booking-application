package com.wissen.hotel.service;

import com.wissen.hotel.dto.response.PriceCalculationResponse;
import com.wissen.hotel.dto.request.PriceSimulationRequest;
import com.wissen.hotel.dto.response.PriceSimulationResult;
import com.wissen.hotel.enums.PricingRuleType;
import com.wissen.hotel.model.Hotel;
import com.wissen.hotel.model.PricingRule;
import com.wissen.hotel.model.Room;
import com.wissen.hotel.repository.PricingRuleRepository;
import com.wissen.hotel.repository.RoomRepository;
import com.wissen.hotel.service.impl.PricingEngineServiceImpl;
import com.wissen.hotel.service.RoomAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
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
    @Mock
    private Clock clock; // <-- Add this

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

        // Re-initialize pricingEngine with the mock clock
        pricingEngine = new PricingEngineServiceImpl(
                roomRepository,
                pricingRuleRepository,
                roomAvailabilityService,
                clock
        );
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
        PricingRule peakRule = createRule(PricingRuleType.PEAK, 15.0);
        PricingRule lastMinuteRule = createRule(PricingRuleType.LAST_MINUTE, 5.0);
        PricingRule weekendRule = createRule(PricingRuleType.WEEKEND, 10.0);

        peakRule.setRuleId(UUID.randomUUID());
        lastMinuteRule.setRuleId(UUID.randomUUID());
        weekendRule.setRuleId(UUID.randomUUID());

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId)).thenReturn(List.of(peakRule, lastMinuteRule, weekendRule));
        when(roomAvailabilityService.getHotelAvailabilityRatio(any(), any(), any())).thenReturn(0.15); // Triggers PEAK

        // Stay: May 31 (Sat) to June 1 (Sun) → 1 night with 1 weekend night
        LocalDate checkIn = LocalDate.of(2025, 5, 31);
        LocalDate checkOut = checkIn.plusDays(1);

        // Mock the clock to 3 days before check-in
        LocalDate mockToday = checkIn.minusDays(3);
        Instant fixedInstant = mockToday.atStartOfDay(ZoneId.systemDefault()).toInstant();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        PriceCalculationResponse response = pricingEngine.calculatePrice(
                roomId,
                checkIn,
                checkOut
        );

        // Expected: 100 + (15% PEAK) + (5% LAST_MINUTE) + (10% WEEKEND) = 130
        assertEquals(new BigDecimal("130.00"), response.getFinalPrice());
        assertTrue(response.getAppliedRuleIds().containsAll(List.of(
                peakRule.getRuleId(),
                lastMinuteRule.getRuleId(),
                weekendRule.getRuleId()
        )));
    }

    @Test
    void calculatePrice_lastMinuteRule_expiredWindow() {
        PricingRule lastMinuteRule = createRule(PricingRuleType.LAST_MINUTE, 5.0);
        
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId)).thenReturn(List.of(lastMinuteRule));
        
        // Mock clock to 4 days before check-in (outside 3-day window)
        LocalDate checkIn = LocalDate.of(2025, 6, 10);
        when(clock.instant()).thenReturn(checkIn.minusDays(4)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        
        PriceCalculationResponse response = pricingEngine.calculatePrice(
                roomId, checkIn, checkIn.plusDays(1));
        
        assertEquals(new BigDecimal("100.00"), response.getFinalPrice());
    }

    @Test
    void applyPricingRules_peakRule_notTriggeredAtThreshold() {
        when(roomAvailabilityService.getHotelAvailabilityRatio(any(), any(), any()))
                .thenReturn(0.25); // Above 0.2 threshold
        
        BigDecimal price = pricingEngine.applyPricingRules(
                new BigDecimal("100.00"),
                List.of(createRule(PricingRuleType.PEAK, 15.0)),
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                hotelId
        );
        
        assertEquals(new BigDecimal("100.00"), price);
    }

    @Test
    void calculatePrice_lastMinuteRule_timeZoneBoundary() {
        PricingRule lastMinuteRule = createRule(PricingRuleType.LAST_MINUTE, 5.0);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId)).thenReturn(List.of(lastMinuteRule));

        // Set clock to UTC+12 timezone
        ZoneId sydZone = ZoneId.of("Pacific/Auckland");
        LocalDate checkIn = LocalDate.of(2025, 6, 10);
        Instant instant = checkIn.atStartOfDay(sydZone).minusDays(3).toInstant();
        
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(sydZone);

        PriceCalculationResponse response = pricingEngine.calculatePrice(
                roomId, checkIn, checkIn.plusDays(1));
        
        assertEquals(new BigDecimal("105.00"), response.getFinalPrice());
    }

    @Test
    void calculatePrice_roomNotFound_throwsException() {
        when(roomRepository.findById(any())).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> 
                pricingEngine.calculatePrice(UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(1)));
    }

    @Test
    void simulatePricing_databaseError_propagatesException() {
        when(roomRepository.findById(any())).thenThrow(new RuntimeException("DB connection failed"));
        
        assertThrows(RuntimeException.class, () ->
                pricingEngine.simulatePricing(new PriceSimulationRequest()));
    }


    @Test
    void simulatePricing_withDateSpecificRule_appliesDiscount() {
        PricingRule dateRule = createDateRule(
                PricingRuleType.DISCOUNT, -5.0,
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 1)
        );

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(pricingRuleRepository.findByHotelAndDateRange(
                eq(hotelId),
                eq(LocalDate.of(2025, 6, 1)),
                eq(LocalDate.of(2025, 6, 1))
        )).thenReturn(List.of(dateRule));
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
    void simulatePricing_mixedRules_sameDay() {
        PricingRule peakRule = createRule(PricingRuleType.PEAK, 10.0);
        PricingRule dateRule = createDateRule(PricingRuleType.DISCOUNT, -5.0,
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 1));
        
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(roomAvailabilityService.getHotelAvailabilityRatio(any(), any(), any()))
                .thenReturn(0.15);
        when(pricingRuleRepository.findByHotelAndDateRange(eq(hotelId), any(), any()))
                .thenReturn(List.of(dateRule));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId))
                .thenReturn(List.of(peakRule));

        List<PriceSimulationResult> results = pricingEngine.simulatePricing(
                PriceSimulationRequest.builder()
                .roomId(roomId)
                .checkIn(LocalDate.of(2025, 6, 1))
                .checkOut(LocalDate.of(2025, 6, 2))
                .build()
        );

        // 100 + 10% (PEAK) -5% (DISCOUNT) = 105
        assertEquals(new BigDecimal("105.00"), results.get(0).getPrice());
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

    @Test
    void applyPricingRules_negativeBasePrice_handled() {
        BigDecimal price = pricingEngine.applyPricingRules(
                new BigDecimal("-100.00"),
                List.of(createRule(PricingRuleType.WEEKEND, 10.0)),
                LocalDate.of(2025, 6, 7), // Saturday
                LocalDate.of(2025, 6, 8),
                hotelId
        );
        
        assertEquals(new BigDecimal("-110.00"), price);
   }

   @Test
void applyPricingRules_dateRangeExclusion() {
    PricingRule dateRule = createDateRule(PricingRuleType.DISCOUNT, -10.0,
        LocalDate.of(2025, 6, 2), LocalDate.of(2025, 6, 4)); // Rule active June 2-4
    
    BigDecimal price = pricingEngine.applyPricingRules(
        new BigDecimal("100.00"),
        List.of(dateRule),
        LocalDate.of(2025, 6, 1), // Check-in before rule
        LocalDate.of(2025, 6, 3), // Check-out during rule
        hotelId
    );
    
    // June 1: no rule, June 2: -10% = 90
    assertEquals(new BigDecimal("190.00"), price);
}

}
