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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface PricingEngineService {
    PriceCalculationResponse calculatePrice(UUID roomId, LocalDate checkIn, LocalDate checkOut);
    List<PriceSimulationResult> simulatePricing(PriceSimulationRequest request);
    BigDecimal applyPricingRules(BigDecimal basePrice, List<PricingRule> rules, LocalDate checkIn, LocalDate checkOut, UUID hotelId);
}