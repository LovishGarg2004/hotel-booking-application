package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.CreateHotelRequest;
import com.wissen.hotel.dtos.HotelResponse;
import com.wissen.hotel.dtos.ReviewResponse;
import com.wissen.hotel.dtos.RoomResponse;
import com.wissen.hotel.dtos.UpdateHotelRequest;
import com.wissen.hotel.services.HotelService;
import com.wissen.hotel.services.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HotelControllerTest {

    private HotelService hotelService;
    private ReviewService reviewService;
    private HotelController controller;

    @BeforeEach
    void setUp() {
        hotelService = mock(HotelService.class);
        reviewService = mock(ReviewService.class);
        controller = new HotelController(hotelService, reviewService);
    }

    @Test
    void getAllHotels_shouldReturnList() {
        List<HotelResponse> hotels = List.of(mock(HotelResponse.class), mock(HotelResponse.class));
        when(hotelService.getAllHotels(null, 0, 10)).thenReturn(hotels);

        ResponseEntity<List<HotelResponse>> result = controller.getAllHotels(null, 0, 10);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(hotels, result.getBody());
        verify(hotelService).getAllHotels(null, 0, 10);
    }

    @Test
    void getHotelById_shouldReturnHotel() {
        UUID id = UUID.randomUUID();
        HotelResponse hotel = mock(HotelResponse.class);
        when(hotelService.getHotelById(id)).thenReturn(hotel);

        ResponseEntity<HotelResponse> result = controller.getHotelById(id);

        assertEquals(200, result.getStatusCode().value());
        assertSame(hotel, result.getBody());
        verify(hotelService).getHotelById(id);
    }

    @Test
    void createHotel_shouldReturnCreatedHotel() {
        CreateHotelRequest request = new CreateHotelRequest();
        HotelResponse hotel = mock(HotelResponse.class);
        when(hotelService.createHotel(request)).thenReturn(hotel);

        ResponseEntity<HotelResponse> result = controller.createHotel(request);

        assertEquals(200, result.getStatusCode().value());
        assertSame(hotel, result.getBody());
        verify(hotelService).createHotel(request);
    }

    @Test
    void updateHotel_shouldReturnUpdatedHotel() {
        UUID id = UUID.randomUUID();
        UpdateHotelRequest request = new UpdateHotelRequest();
        HotelResponse hotel = mock(HotelResponse.class);
        when(hotelService.updateHotel(id, request)).thenReturn(hotel);

        ResponseEntity<HotelResponse> result = controller.updateHotel(id, request);

        assertEquals(200, result.getStatusCode().value());
        assertSame(hotel, result.getBody());
        verify(hotelService).updateHotel(id, request);
    }

    @Test
    void deleteHotel_shouldReturnNoContent() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> result = controller.deleteHotel(id);

        assertEquals(204, result.getStatusCode().value());
        verify(hotelService).deleteHotel(id);
    }

    @Test
    void approveHotel_shouldReturnApprovedHotel() {
        UUID id = UUID.randomUUID();
        HotelResponse hotel = mock(HotelResponse.class);
        when(hotelService.approveHotel(id)).thenReturn(hotel);

        ResponseEntity<HotelResponse> result = controller.approveHotel(id);

        assertEquals(200, result.getStatusCode().value());
        assertSame(hotel, result.getBody());
        verify(hotelService).approveHotel(id);
    }

    @Test
    void searchHotels_shouldReturnList() {
        List<HotelResponse> hotels = List.of(mock(HotelResponse.class), mock(HotelResponse.class));
        String city = "City";
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(1);
        int guests = 2, page = 0, size = 10;
        when(hotelService.searchHotels(city, checkIn, checkOut, guests, page, size)).thenReturn(hotels);

        ResponseEntity<List<HotelResponse>> result = controller.searchHotels(city, checkIn, checkOut, guests, page, size);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(hotels, result.getBody());
        verify(hotelService).searchHotels(city, checkIn, checkOut, guests, page, size);
    }

    @Test
    void getTopRatedHotels_shouldReturnList() {
        List<HotelResponse> hotels = List.of(mock(HotelResponse.class), mock(HotelResponse.class));
        when(hotelService.getTopRatedHotels()).thenReturn(hotels);

        ResponseEntity<List<HotelResponse>> result = controller.getTopRatedHotels();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(hotels, result.getBody());
        verify(hotelService).getTopRatedHotels();
    }

    @Test
    void getNearbyHotels_shouldReturnList() {
        List<HotelResponse> hotels = List.of(mock(HotelResponse.class), mock(HotelResponse.class));
        double lat = 1.0, lon = 2.0, radius = 10.0;
        when(hotelService.findNearbyHotels(lat, lon, radius)).thenReturn(hotels);

        ResponseEntity<List<HotelResponse>> result = controller.getNearbyHotels(lat, lon, radius);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(hotels, result.getBody());
        verify(hotelService).findNearbyHotels(lat, lon, radius);
    }

    @Test
    void getHotelRooms_shouldReturnRooms() {
        UUID id = UUID.randomUUID();
        List<RoomResponse> rooms = List.of(mock(RoomResponse.class), mock(RoomResponse.class));
        when(hotelService.getHotelRooms(id)).thenReturn(rooms);

        ResponseEntity<?> result = controller.getHotelRooms(id);

        assertEquals(200, result.getStatusCode().value());
        assertSame(rooms, result.getBody());
        verify(hotelService).getHotelRooms(id);
    }

    @Test
    void getReviewsByHotel_shouldReturnList() {
        UUID hotelId = UUID.randomUUID();
        List<ReviewResponse> reviews = List.of(mock(ReviewResponse.class), mock(ReviewResponse.class));
        when(reviewService.getReviewsByHotel(hotelId)).thenReturn(reviews);

        ResponseEntity<List<ReviewResponse>> result = controller.getReviewsByHotel(hotelId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(reviews, result.getBody());
        verify(reviewService).getReviewsByHotel(hotelId);
    }

    @Test
    void checkHotelAvailability_shouldReturnAvailability() {
        UUID id = UUID.randomUUID();
        Object availability = new Object();
        String checkIn = "2024-01-01", checkOut = "2024-01-02";
        when(hotelService.checkAvailability(id, checkIn, checkOut)).thenReturn(availability);

        ResponseEntity<?> result = controller.checkHotelAvailability(id, checkIn, checkOut);

        assertEquals(200, result.getStatusCode().value());
        assertSame(availability, result.getBody());
        verify(hotelService).checkAvailability(id, checkIn, checkOut);
    }

    @Test
    void getHotelsOwnedByUser_shouldReturnList() {
        List<HotelResponse> hotels = List.of(mock(HotelResponse.class), mock(HotelResponse.class));
        when(hotelService.getHotelsOwnedByCurrentUser()).thenReturn(hotels);

        ResponseEntity<List<HotelResponse>> result = controller.getHotelsOwnedByUser();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(hotels, result.getBody());
        verify(hotelService).getHotelsOwnedByCurrentUser();
    }

    @Test
    void getAverageRating_shouldReturnRating() {
        UUID hotelId = UUID.randomUUID();
        double rating = 4.5;
        when(hotelService.getAverageRating(hotelId)).thenReturn(rating);

        ResponseEntity<Double> result = controller.getAverageRating(hotelId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(rating, result.getBody());
        verify(hotelService).getAverageRating(hotelId);
    }
}