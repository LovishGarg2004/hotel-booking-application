package com.wissen.hotel.service;

import com.wissen.hotel.dto.request.*;
import com.wissen.hotel.dto.response.*;
import com.wissen.hotel.enums.PricingRuleType;
import com.wissen.hotel.model.PricingRule;
import com.wissen.hotel.model.Room;
import com.wissen.hotel.repository.PricingRuleRepository;
import com.wissen.hotel.repository.RoomRepository;
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