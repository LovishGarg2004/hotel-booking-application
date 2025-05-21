package com.wissen.hotel.services;

import com.wissen.hotel.dtos.*;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponse createBooking(CreateBookingRequest request);
    BookingResponse getBookingById(UUID bookingId);
    BookingResponse updateBooking(UUID bookingId, UpdateBookingRequest request);
    BookingResponse cancelBooking(UUID bookingId);
    List<BookingResponse> getAllBookings(String filter); // Admin only
    List<BookingResponse> getBookingsForHotel(UUID hotelId);
    BookingResponse generateInvoice(UUID bookingId);
}
