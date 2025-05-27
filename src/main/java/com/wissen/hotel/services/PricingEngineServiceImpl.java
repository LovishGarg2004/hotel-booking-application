package com.wissen.hotel.services;

import com.wissen.hotel.dtos.PriceCalculationResponse;
import com.wissen.hotel.dtos.PriceSimulationRequest;
import com.wissen.hotel.dtos.PriceSimulationResult;
import com.wissen.hotel.enums.PricingRuleType;
import com.wissen.hotel.exceptions.EntityNotFoundException;
import com.wissen.hotel.models.PricingRule;
import com.wissen.hotel.models.Room;
import com.wissen.hotel.repositories.PricingRuleRepository;
import com.wissen.hotel.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingEngineServiceImpl implements PricingEngineService {
    private static final Logger log = LoggerFactory.getLogger(PricingEngineServiceImpl.class);
    private final RoomRepository roomRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final RoomAvailabilityService roomAvailabilityService;

    @Override
    public PriceCalculationResponse calculatePrice(UUID roomId, LocalDate checkIn, LocalDate checkOut) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        BigDecimal basePrice = room.getBasePrice();
        UUID hotelId = room.getHotel().getHotelId();

        // Fetch rules: date-agnostic for WEEKEND, PEAK, LAST_MINUTE; date-specific for others
        List<PricingRule> dateAgnosticRules = pricingRuleRepository.findByHotel_HotelId(hotelId).stream()
            .filter(rule -> rule.getRuleType() == PricingRuleType.WEEKEND || rule.getRuleType() == PricingRuleType.PEAK || rule.getRuleType() == PricingRuleType.LAST_MINUTE)
            .toList();
        List<PricingRule> dateSpecificRules = pricingRuleRepository.findByHotelAndDateRange(hotelId, checkIn, checkOut).stream()
            .filter(rule -> rule.getRuleType() != PricingRuleType.WEEKEND && rule.getRuleType() != PricingRuleType.PEAK && rule.getRuleType() != PricingRuleType.LAST_MINUTE)
            .toList();

        // Combine rules and ensure no duplicates
        Set<PricingRule> combinedRules = new HashSet<>(dateAgnosticRules);
        combinedRules.addAll(dateSpecificRules);

        // Apply all applicable percentage increases
        BigDecimal finalPrice = applyPricingRules(basePrice, new ArrayList<>(combinedRules), checkIn, checkOut, hotelId);

        List<UUID> ruleIds = combinedRules.stream().map(PricingRule::getRuleId).toList();
        PriceCalculationResponse response = new PriceCalculationResponse();
        response.setRoomId(roomId);
        response.setCheckIn(checkIn);
        response.setCheckOut(checkOut);
        response.setBasePrice(basePrice);
        response.setFinalPrice(finalPrice);
        response.setAppliedRuleIds(ruleIds);
        return response;
    }

    @Override
    public List<PriceSimulationResult> simulatePricing(PriceSimulationRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        UUID hotelId = room.getHotel().getHotelId();
        List<PriceSimulationResult> results = new ArrayList<>();

        for (LocalDate date = request.getCheckIn(); !date.isAfter(request.getCheckOut().minusDays(1)); date = date.plusDays(1)) {
            // Fetch rules for this date: date-agnostic for WEEKEND, PEAK, LAST_MINUTE; date-specific for others
            List<PricingRule> dateAgnosticRules = pricingRuleRepository.findByHotel_HotelId(hotelId).stream()
                .filter(rule -> rule.getRuleType() == PricingRuleType.WEEKEND || rule.getRuleType() == PricingRuleType.PEAK || rule.getRuleType() == PricingRuleType.LAST_MINUTE)
                .toList();
            List<PricingRule> dateSpecificRules = pricingRuleRepository.findByHotelAndDateRange(hotelId, date, date).stream()
                .filter(rule -> rule.getRuleType() != PricingRuleType.WEEKEND && rule.getRuleType() != PricingRuleType.PEAK && rule.getRuleType() != PricingRuleType.LAST_MINUTE)
                .toList();

            // Combine rules and ensure no duplicates
            Set<PricingRule> combinedRules = new HashSet<>(dateAgnosticRules);
            combinedRules.addAll(dateSpecificRules);

            BigDecimal price = applyPricingRules(room.getBasePrice(), new ArrayList<>(combinedRules), date, date.plusDays(1), hotelId);
            List<UUID> ruleIds = combinedRules.stream().map(PricingRule::getRuleId).toList();
            PriceSimulationResult result = new PriceSimulationResult();
            result.setDate(date);
            result.setPrice(price);
            result.setAppliedRuleIds(ruleIds);
            results.add(result);
        }
        return results;
    }

    @Override
    public BigDecimal applyPricingRules(
            BigDecimal basePrice,
            List<PricingRule> rules,
            LocalDate checkIn,
            LocalDate checkOut,
            UUID hotelId
    ) {
        BigDecimal price = basePrice;
        log.info("Applying pricing rules for hotelId={}, checkIn={}, checkOut={}, basePrice={}", hotelId, checkIn, checkOut, basePrice);

        // Create a mutable copy of the rules list before sorting
        List<PricingRule> mutableRules = new ArrayList<>(rules);
        mutableRules.sort((r1, r2) -> r1.getRuleType().compareTo(r2.getRuleType()));

        for (PricingRule rule : mutableRules) {
            price = applyRule(price, rule, checkIn, checkOut, hotelId);
        }

        log.info("Final calculated price after applying all rules: {}", price.setScale(2, RoundingMode.HALF_UP));
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal applyRule(BigDecimal price, PricingRule rule, LocalDate checkIn, LocalDate checkOut, UUID hotelId) {
        switch (rule.getRuleType()) {
            case PEAK:
                return applyPeakRule(price, rule, checkIn, checkOut, hotelId);
            case WEEKEND:
                return applyWeekendRule(price, rule, checkIn, checkOut);
            case LAST_MINUTE:
                return applyLastMinuteRule(price, rule, checkIn);
            default:
                return applyGenericRule(price, rule);
        }
    }

    private BigDecimal applyPeakRule(BigDecimal price, PricingRule rule, LocalDate checkIn, LocalDate checkOut, UUID hotelId) {
        if (rule.getRuleValue() != null && rule.getRuleValue() > 0) {
            double availabilityRatio = roomAvailabilityService.getHotelAvailabilityRatio(hotelId, checkIn, checkOut);
            if (availabilityRatio < 0.2) {
                BigDecimal increment = price.multiply(BigDecimal.valueOf(rule.getRuleValue())).divide(BigDecimal.valueOf(100));
                price = price.add(increment);
            }
        }
        return price;
    }

    private BigDecimal applyWeekendRule(BigDecimal price, PricingRule rule, LocalDate checkIn, LocalDate checkOut) {
        if (rule.getRuleValue() != null && rule.getRuleValue() > 0) {
            int weekendDays = 0;
            int totalDays = 0;
            for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
                DayOfWeek dow = date.getDayOfWeek();
                if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                    weekendDays++;
                }
                totalDays++;
            }
            if (weekendDays > 0 && totalDays > 0) {
                BigDecimal weekendRatio = BigDecimal.valueOf(weekendDays).divide(BigDecimal.valueOf(totalDays), 4, RoundingMode.HALF_UP);
                BigDecimal increment = price.multiply(BigDecimal.valueOf(rule.getRuleValue()))
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                        .multiply(weekendRatio);
                price = price.add(increment);
            }
        }
        return price;
    }

    private BigDecimal applyLastMinuteRule(BigDecimal price, PricingRule rule, LocalDate checkIn) {
        if (rule.getRuleValue() != null && rule.getRuleValue() > 0) {
            long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), checkIn);
            if (daysUntilCheckIn >= 0 && daysUntilCheckIn <= 3) {
                BigDecimal increment = price.multiply(BigDecimal.valueOf(rule.getRuleValue())).divide(BigDecimal.valueOf(100));
                price = price.add(increment);
            }
        }
        return price;
    }

    private BigDecimal applyGenericRule(BigDecimal price, PricingRule rule) {
        if (rule.getRuleValue() != null && rule.getRuleValue() != 0) {
            BigDecimal increment = price.multiply(BigDecimal.valueOf(rule.getRuleValue())).divide(BigDecimal.valueOf(100));
            price = price.add(increment);
        }
        return price;
    }
}
