package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.HotelAnalyticsResponse;
import com.wissen.hotel.services.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyticsControllerTest {

    private AnalyticsService analyticsService;
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        analyticsService = mock(AnalyticsService.class);
        analyticsController = new AnalyticsController(analyticsService);
    }

    @Test
    void getHotelAnalytics_shouldReturnOkWithResponse() {
        UUID hotelId = UUID.randomUUID();
        HotelAnalyticsResponse response = new HotelAnalyticsResponse();
        when(analyticsService.getHotelAnalytics(hotelId)).thenReturn(response);

        ResponseEntity<HotelAnalyticsResponse> result = analyticsController.getHotelAnalytics(hotelId);

        assertEquals(200, result.getStatusCodeValue());
        assertSame(response, result.getBody());
        verify(analyticsService, times(1)).getHotelAnalytics(hotelId);
    }

    @Test
    void getHotelAnalytics_shouldReturnOkWithNullBodyWhenServiceReturnsNull() {
        UUID hotelId = UUID.randomUUID();
        when(analyticsService.getHotelAnalytics(hotelId)).thenReturn(null);

        ResponseEntity<HotelAnalyticsResponse> result = analyticsController.getHotelAnalytics(hotelId);

        assertEquals(200, result.getStatusCodeValue());
        assertNull(result.getBody());
        verify(analyticsService, times(1)).getHotelAnalytics(hotelId);
    }

    @Test
    void getHotelAnalytics_shouldCallServiceWithCorrectHotelId() {
        UUID hotelId = UUID.randomUUID();
        analyticsController.getHotelAnalytics(hotelId);
        verify(analyticsService).getHotelAnalytics(hotelId);
    }
}
