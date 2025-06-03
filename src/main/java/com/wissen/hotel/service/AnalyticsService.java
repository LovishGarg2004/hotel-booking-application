package com.wissen.hotel.service;

import com.wissen.hotel.dto.response.HotelAnalyticsResponse;
import java.util.UUID;

public interface AnalyticsService {
    HotelAnalyticsResponse getHotelAnalytics(UUID hotelId);
}
