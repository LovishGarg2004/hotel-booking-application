package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.PriceCalculationResponse;
import com.wissen.hotel.dtos.PriceSimulationRequest;
import com.wissen.hotel.dtos.PriceSimulationResult;
import com.wissen.hotel.services.PricingEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PriceCalculationControllerTest {

    private PricingEngineService pricingEngineService;
    private PriceCalculationController controller;

    @BeforeEach
    void setUp() {
        pricingEngineService = mock(PricingEngineService.class);
        controller = new PriceCalculationController(pricingEngineService);
    }

    @Test
    void calculatePrice_shouldReturnOkWithResponse() {
        UUID roomId = UUID.randomUUID();
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(2);
        PriceCalculationResponse response = new PriceCalculationResponse();
        when(pricingEngineService.calculatePrice(roomId, checkIn, checkOut)).thenReturn(response);

        ResponseEntity<PriceCalculationResponse> result = controller.calculatePrice(roomId, checkIn, checkOut);

        assertEquals(200, result.getStatusCodeValue());
        assertSame(response, result.getBody());
        verify(pricingEngineService, times(1)).calculatePrice(roomId, checkIn, checkOut);
    }

    @Test
    void simulatePricing_shouldReturnOkWithList() {
        PriceSimulationRequest request = new PriceSimulationRequest();
        List<PriceSimulationResult> simulationResults = Arrays.asList(new PriceSimulationResult(), new PriceSimulationResult());
        when(pricingEngineService.simulatePricing(request)).thenReturn(simulationResults);

        ResponseEntity<List<PriceSimulationResult>> result = controller.simulatePricing(request);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(simulationResults, result.getBody());
        verify(pricingEngineService, times(1)).simulatePricing(request);
    }
}