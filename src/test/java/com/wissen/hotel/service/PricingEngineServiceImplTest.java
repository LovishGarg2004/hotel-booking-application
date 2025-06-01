package com.wissen.hotel.service;

import com.wissen.hotel.dtos.PriceCalculationResponse;
import com.wissen.hotel.dtos.PriceSimulationRequest;
import com.wissen.hotel.dtos.PriceSimulationResult;
import com.wissen.hotel.enums.PricingRuleType;
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
