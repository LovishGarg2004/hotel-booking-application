package com.wissen.hotel.service.impl;

import com.wissen.hotel.dto.response.*;
import com.wissen.hotel.dto.request.PriceSimulationRequest;

import com.wissen.hotel.enums.PricingRuleType;
import com.wissen.hotel.model.PricingRule;
import com.wissen.hotel.model.Room;
import com.wissen.hotel.repository.PricingRuleRepository;
import com.wissen.hotel.repository.RoomRepository;
import com.wissen.hotel.service.PricingEngineService;
import com.wissen.hotel.service.RoomAvailabilityService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
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
    private final Clock clock; // <-- Inject Clock

    @Override
    public PriceCalculationResponse calculatePrice(UUID roomId, LocalDate checkIn, LocalDate checkOut) {
        try {
            Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
            BigDecimal basePrice = room.getBasePrice();
            UUID hotelId = room.getHotel().getHotelId();
            List<PricingRule> dateAgnosticRules = pricingRuleRepository.findByHotel_HotelId(hotelId).stream()
                .filter(rule -> rule.getRuleType() == PricingRuleType.WEEKEND || rule.getRuleType() == PricingRuleType.PEAK || rule.getRuleType() == PricingRuleType.LAST_MINUTE)
                .toList();
            List<PricingRule> dateSpecificRules = pricingRuleRepository.findByHotelAndDateRange(hotelId, checkIn, checkOut).stream()
                .filter(rule -> rule.getRuleType() != PricingRuleType.WEEKEND && rule.getRuleType() != PricingRuleType.PEAK && rule.getRuleType() != PricingRuleType.LAST_MINUTE)
                .toList();
            List<PricingRule> rules = new ArrayList<>();
            rules.addAll(dateAgnosticRules);
            rules.addAll(dateSpecificRules);

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
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate price. Please try again later.", e);
        }
    }

    @Override
    public List<PriceSimulationResult> simulatePricing(PriceSimulationRequest request) {
        try {
            Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
            UUID hotelId = room.getHotel().getHotelId();
            List<PriceSimulationResult> results = new ArrayList<>();
            for (LocalDate date = request.getCheckIn(); !date.isAfter(request.getCheckOut().minusDays(1)); date = date.plusDays(1)) {
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to simulate pricing. Please try again later.", e);
        }
    }

    public BigDecimal applyPricingRules(
        BigDecimal basePrice,
        List<PricingRule> rules,
        LocalDate checkIn,
        LocalDate checkOut,
        UUID hotelId
    ) {
        BigDecimal totalPrice = BigDecimal.ZERO;

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
                        long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(clock), checkIn); // <-- Use injected clock
                        if (daysUntilCheckIn >= 0 && daysUntilCheckIn <= 3 && rule.getRuleValue() != null && rule.getRuleValue() > 0) {
                            dayPrice = dayPrice.add(basePrice.multiply(BigDecimal.valueOf(rule.getRuleValue())).divide(BigDecimal.valueOf(100)));
                        }
                    }
                    default -> {
                        if (rule.getStartDate() != null && rule.getEndDate() != null
                                && (date.isBefore(rule.getStartDate()) || date.isAfter(rule.getEndDate()))) {
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
