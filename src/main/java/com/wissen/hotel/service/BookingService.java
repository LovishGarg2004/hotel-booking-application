package com.wissen.hotel.service;

import com.wissen.hotel.dto.request.*;
import com.wissen.hotel.dto.response.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponse createBooking(CreateBookingRequest request);
    BookingResponse getBookingById(UUID bookingId);
    BookingResponse updateBooking(UUID bookingId, UpdateBookingRequest request);
    BookingResponse approveBooking(UUID bookingId);
    BookingResponse cancelBooking(UUID bookingId);
    List<BookingResponse> getAllBookings(String filter); // Admin only
    List<BookingResponse> getBookingsForHotel(UUID hotelId);
    BookingResponse generateInvoice(UUID bookingId);
    boolean isRoomAvailable(UUID roomId, LocalDate checkIn, LocalDate checkOut);
}
