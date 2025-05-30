package com.wissen.hotel.services;

import com.wissen.hotel.dtos.HotelAnalyticsResponse;
import java.util.UUID;

public interface AnalyticsService {
    HotelAnalyticsResponse getHotelAnalytics(UUID hotelId);
}
