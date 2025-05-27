package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.HotelAnalyticsResponse;
import com.wissen.hotel.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/hotel")
    public ResponseEntity<HotelAnalyticsResponse> getHotelAnalytics(@RequestParam UUID hotelId) {
        return ResponseEntity.ok(analyticsService.getHotelAnalytics(hotelId));
    }
}
