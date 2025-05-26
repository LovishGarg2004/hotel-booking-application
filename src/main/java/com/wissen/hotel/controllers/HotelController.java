package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.CreateHotelRequest;
import com.wissen.hotel.dtos.HotelResponse;
import com.wissen.hotel.dtos.ReviewResponse;
import com.wissen.hotel.dtos.UpdateHotelRequest;
import com.wissen.hotel.services.HotelService;
import com.wissen.hotel.services.ReviewService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin("http://localhost:8080")
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<HotelResponse>> getAllHotels(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(hotelService.getAllHotels(city, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable UUID id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(@RequestBody CreateHotelRequest request) {
        return ResponseEntity.ok(hotelService.createHotel(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelResponse> updateHotel(@PathVariable UUID id, @RequestBody UpdateHotelRequest request) {
        return ResponseEntity.ok(hotelService.updateHotel(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable UUID id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<HotelResponse> approveHotel(@PathVariable UUID id) {
        return ResponseEntity.ok(hotelService.approveHotel(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<HotelResponse>> searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) LocalDate checkIn,
            @RequestParam(required = false) LocalDate checkOut,
            @RequestParam(required = false) Integer numberOfGuests,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(hotelService.searchHotels(city, checkIn, checkOut, numberOfGuests, page, size));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<HotelResponse>> getTopRatedHotels() {
        return ResponseEntity.ok(hotelService.getTopRatedHotels());
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<HotelResponse>> getNearbyHotels(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10.0") double radiusKm) {
        return ResponseEntity.ok(hotelService.findNearbyHotels(latitude, longitude, radiusKm));
    }

    @GetMapping("/{id}/rooms")
    public ResponseEntity<?> getHotelRooms(@PathVariable UUID id) {
        return ResponseEntity.ok(hotelService.getHotelRooms(id));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviewsByHotel(@PathVariable UUID hotelId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByHotel(hotelId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<?> checkHotelAvailability(
            @PathVariable UUID id,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {
        return ResponseEntity.ok(hotelService.checkAvailability(id, checkIn, checkOut));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<HotelResponse>> getHotelsOwnedByUser() {
        return ResponseEntity.ok(hotelService.getHotelsOwnedByCurrentUser());
    }
}
