package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable UUID id,
                                                         @RequestBody UpdateBookingRequest request) {
        return ResponseEntity.ok(bookingService.updateBooking(id, request));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings(@RequestParam(required = false) String filter) {
        return ResponseEntity.ok(bookingService.getAllBookings(filter));
    }

    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<List<BookingResponse>> getHotelBookings(@PathVariable UUID hotelId) {
        return ResponseEntity.ok(bookingService.getBookingsForHotel(hotelId));
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<BookingResponse> generateInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.generateInvoice(id));
    }
}
