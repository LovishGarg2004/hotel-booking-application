package com.wissen.hotel.services;

import com.wissen.hotel.dtos.PriceCalculationResponse;
import com.wissen.hotel.dtos.PriceSimulationRequest;
import com.wissen.hotel.dtos.PriceSimulationResult;
import com.wissen.hotel.enums.PricingRuleType;
import com.wissen.hotel.models.PricingRule;
import com.wissen.hotel.models.Room;
import com.wissen.hotel.repositories.PricingRuleRepository;
import com.wissen.hotel.repositories.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.List;
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
        List<PricingRule> rules = new ArrayList<>();
        rules.addAll(dateAgnosticRules);
        rules.addAll(dateSpecificRules);

        // Apply all applicable percentage increases
        BigDecimal finalPrice = applyPricingRules(basePrice, rules, checkIn, checkOut, hotelId);

        List<UUID> ruleIds = rules.stream().map(PricingRule::getRuleId).toList();
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
            List<PricingRule> rules = new ArrayList<>();
            rules.addAll(dateAgnosticRules);
            rules.addAll(dateSpecificRules);

            BigDecimal price = applyPricingRules(room.getBasePrice(), rules, date, date.plusDays(1), hotelId);
            List<UUID> ruleIds = rules.stream().map(PricingRule::getRuleId).toList();
            PriceSimulationResult result = new PriceSimulationResult();
            result.setDate(date);
            result.setPrice(price);
            result.setAppliedRuleIds(ruleIds);
            results.add(result);
        }
        return results;
    }

    public BigDecimal applyPricingRules(
        BigDecimal basePrice,
        List<PricingRule> rules,
        LocalDate checkIn,
        LocalDate checkOut,
        UUID hotelId
) {
    BigDecimal totalPrice = BigDecimal.ZERO;
    long numDays = ChronoUnit.DAYS.between(checkIn, checkOut);

    for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
        BigDecimal dayPrice = basePrice;

        for (PricingRule rule : rules) {
            switch (rule.getRuleType()) {
                case PEAK -> {
                    double availabilityRatio = roomAvailabilityService.getHotelAvailabilityRatio(hotelId, date, date.plusDays(1));
                    if (availabilityRatio < 0.2 && rule.getRuleValue() != null && rule.getRuleValue() > 0) {
                        dayPrice = dayPrice.add(basePrice.multiply(BigDecimal.valueOf(rule.getRuleValue())).divide(BigDecimal.valueOf(100)));
                    }
                }
                case WEEKEND -> {
                    DayOfWeek dow = date.getDayOfWeek();
                    if ((dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) && rule.getRuleValue() != null && rule.getRuleValue() > 0) {
                        dayPrice = dayPrice.add(basePrice.multiply(BigDecimal.valueOf(rule.getRuleValue())).divide(BigDecimal.valueOf(100)));
                    }
                }
                case LAST_MINUTE -> {
                    long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), checkIn);
                    if (daysUntilCheckIn >= 0 && daysUntilCheckIn <= 3 && rule.getRuleValue() != null && rule.getRuleValue() > 0) {
                        dayPrice = dayPrice.add(basePrice.multiply(BigDecimal.valueOf(rule.getRuleValue())).divide(BigDecimal.valueOf(100)));
                    }
                }
                default -> {
                    // For SEASONAL, DISCOUNT, etc., check if this rule applies to this date
                    if (rule.getStartDate() != null && rule.getEndDate() != null
                            && (date.isBefore(rule.getStartDate()) || date.isAfter(rule.getEndDate()))) {
                        // Rule does not apply to this date
                        continue;
                    }
                    if (rule.getRuleValue() != null && rule.getRuleValue() != 0) {
                        dayPrice = dayPrice.add(basePrice.multiply(BigDecimal.valueOf(rule.getRuleValue())).divide(BigDecimal.valueOf(100)));
                    }
                }
            }
        }
        totalPrice = totalPrice.add(dayPrice);
    }

    return totalPrice.setScale(2, RoundingMode.HALF_UP);
}
}
