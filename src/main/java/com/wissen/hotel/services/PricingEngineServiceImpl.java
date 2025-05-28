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
        // 1. PEAK Rule: Apply if average hotel availability is below threshold (e.g., 20%)
        for (PricingRule rule : rules) {
            if (rule.getRuleType() == PricingRuleType.PEAK && rule.getRuleValue() != null && rule.getRuleValue() > 0) {
                double availabilityRatio = roomAvailabilityService.getHotelAvailabilityRatio(hotelId, checkIn, checkOut);
                log.info("Evaluating PEAK rule: ruleId={}, value={}, availabilityRatio={}", rule.getRuleId(), rule.getRuleValue(), availabilityRatio);
                if (availabilityRatio < 0.2) { // 20% threshold
                    BigDecimal increment = basePrice.multiply(BigDecimal.valueOf(rule.getRuleValue())).divide(BigDecimal.valueOf(100));
                    price = price.add(increment);
                    log.info("Applied PEAK rule: ruleId={}, increment={}, newPrice={}", rule.getRuleId(), increment, price);
                }
            }
        }

        // 2. WEEKEND Rule: Apply for each weekend day in the booking range
        for (PricingRule rule : rules) {
            if (rule.getRuleType() == PricingRuleType.WEEKEND && rule.getRuleValue() != null && rule.getRuleValue() > 0) {
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
                    BigDecimal increment = basePrice.multiply(BigDecimal.valueOf(rule.getRuleValue()))
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                            .multiply(weekendRatio);
                    price = price.add(increment);
                    log.info("Applied WEEKEND rule: ruleId={}, value={}, weekendDays={}, totalDays={}, increment={}, newPrice={}",
                        rule.getRuleId(), rule.getRuleValue(), weekendDays, totalDays, increment, price);
                }
            }
        }

        // 3. LAST_MINUTE Rule: Apply if booking is within 3 days of check-in
        for (PricingRule rule : rules) {
            if (rule.getRuleType() == PricingRuleType.LAST_MINUTE && rule.getRuleValue() != null && rule.getRuleValue() > 0) {
                long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), checkIn);
                log.info("Evaluating LAST_MINUTE rule: ruleId={}, value={}, daysUntilCheckIn={}", rule.getRuleId(), rule.getRuleValue(), daysUntilCheckIn);
                if (daysUntilCheckIn >= 0 && daysUntilCheckIn <= 3) {
                    BigDecimal increment = basePrice.multiply(BigDecimal.valueOf(rule.getRuleValue())).divide(BigDecimal.valueOf(100));
                    price = price.add(increment);
                    log.info("Applied LAST_MINUTE rule: ruleId={}, increment={}, newPrice={}", rule.getRuleId(), increment, price);
                }
            }
        }

        // 4. Other Rules (e.g., SEASONAL, DISCOUNT)
        for (PricingRule rule : rules) {
            if (rule.getRuleType() != PricingRuleType.PEAK &&
                rule.getRuleType() != PricingRuleType.WEEKEND &&
                rule.getRuleType() != PricingRuleType.LAST_MINUTE &&
                rule.getRuleValue() != null && rule.getRuleValue() != 0) {
                BigDecimal increment = basePrice.multiply(BigDecimal.valueOf(rule.getRuleValue())).divide(BigDecimal.valueOf(100));
                price = price.add(increment);
                log.info("Applied {} rule: ruleId={}, value={}, increment={}, newPrice={}",
                    rule.getRuleType(), rule.getRuleId(), rule.getRuleValue(), increment, price);
            }
        }

        log.info("Final calculated price: {}", price.setScale(2, RoundingMode.HALF_UP));
        return price.setScale(2, RoundingMode.HALF_UP);
    }
}
