package com.wissen.hotel.controller;

import com.wissen.hotel.dto.response.HotelAnalyticsResponse;
import com.wissen.hotel.service.AnalyticsService;
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
