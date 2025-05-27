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
    private PricingEngineServiceImpl pricingEngineService;

    private UUID roomId;
    private UUID hotelId;
    private Room room;
    private PricingRule peakRule;
    private PricingRule weekendRule;
    private PricingRule lastMinuteRule;
    private PricingRule seasonalRule;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        hotelId = UUID.randomUUID();

        Hotel hotel = new Hotel();
        hotel.setHotelId(hotelId);

        room = new Room();
        room.setRoomId(roomId);
        room.setBasePrice(BigDecimal.valueOf(100));
        room.setHotel(hotel);

        peakRule = new PricingRule();
        peakRule.setRuleType(PricingRuleType.PEAK);
        peakRule.setRuleValue(20);
        peakRule.setStartDate(LocalDate.now().minusDays(1));
        peakRule.setEndDate(LocalDate.now().plusDays(10));

        weekendRule = new PricingRule();
        weekendRule.setRuleType(PricingRuleType.WEEKEND);
        weekendRule.setRuleValue(15);

        lastMinuteRule = new PricingRule();
        lastMinuteRule.setRuleType(PricingRuleType.LAST_MINUTE);
        lastMinuteRule.setRuleValue(10);

        seasonalRule = new PricingRule();
        seasonalRule.setRuleType(PricingRuleType.SEASONAL);
        seasonalRule.setRuleValue(5);
        seasonalRule.setStartDate(LocalDate.now().minusDays(1));
        seasonalRule.setEndDate(LocalDate.now().plusDays(10));
    }

    @Test
    void calculatePrice_ShouldApplyAllRules() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);
    
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId))
                .thenReturn(List.of(peakRule, weekendRule, lastMinuteRule));
        when(pricingRuleRepository.findByHotelAndDateRange(eq(hotelId), any(), any()))
                .thenReturn(List.of(seasonalRule)); // Check if this is actually used
        when(roomAvailabilityService.getHotelAvailabilityRatio(eq(hotelId), any(), any()))
                .thenReturn(0.15);
    
        PriceCalculationResponse response = pricingEngineService.calculatePrice(roomId, checkIn, checkOut);
    
        System.out.println("Final Price: " + response.getFinalPrice());
        System.out.println("Applied Rules Count: " + response.getAppliedRuleIds().size());
    
        // If only 3 rules apply
        BigDecimal expected = BigDecimal.valueOf(145.53); // 100 * 1.20 * 1.15 * 1.10
        assertEquals(expected.setScale(2), response.getFinalPrice());
    }
    
    @Test
    void calculatePrice_ShouldThrowWhenRoomNotFound() {
        UUID roomId = UUID.randomUUID();
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(1);

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
            pricingEngineService.calculatePrice(roomId, checkIn, checkOut));

        assertEquals("Room not found", exception.getMessage());
    }

    @Test
    void simulatePricing_ShouldReturnDailyPrices() {
        LocalDate checkIn = LocalDate.now().plusDays(5); // Assume Friday
        LocalDate checkOut = checkIn.plusDays(3); // Friday to Sunday

        PriceSimulationRequest request = new PriceSimulationRequest();
        request.setRoomId(roomId);
        request.setCheckIn(checkIn);
        request.setCheckOut(checkOut);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(pricingRuleRepository.findByHotel_HotelId(hotelId))
                .thenReturn(List.of(peakRule, weekendRule, lastMinuteRule));
        when(pricingRuleRepository.findByHotelAndDateRange(eq(hotelId), any(), any()))
                .thenReturn(List.of(seasonalRule));
        when(roomAvailabilityService.getHotelAvailabilityRatio(eq(hotelId), any(), any()))
                .thenReturn(0.15);

        List<PriceSimulationResult> results = pricingEngineService.simulatePricing(request);

        assertEquals(3, results.size());
        for (PriceSimulationResult result : results) {
            assertNotNull(result.getPrice());
            assertTrue(result.getPrice().compareTo(BigDecimal.ZERO) > 0);
            assertEquals(4, result.getAppliedRuleIds().size());
        }
    }
}
