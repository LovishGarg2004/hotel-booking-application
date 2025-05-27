package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.PriceCalculationResponse;
import com.wissen.hotel.dtos.PriceSimulationRequest;
import com.wissen.hotel.dtos.PriceSimulationResult;
import com.wissen.hotel.services.PricingEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PriceCalculationController {
    private final PricingEngineService pricingEngineService;

    @GetMapping("/calculate")
    public ResponseEntity<PriceCalculationResponse> calculatePrice(
        @RequestParam UUID roomId,
        @RequestParam LocalDate checkIn,
        @RequestParam LocalDate checkOut
    ) {
        return ResponseEntity.ok(pricingEngineService.calculatePrice(roomId, checkIn, checkOut));
    }

    @PostMapping("/simulate")
    @PreAuthorize("hasAnyRole('HOTEL_OWNER', 'ADMIN')")
    public ResponseEntity<List<PriceSimulationResult>> simulatePricing(
        @RequestBody PriceSimulationRequest request
    ) {
        return ResponseEntity.ok(pricingEngineService.simulatePricing(request));
    }
}

