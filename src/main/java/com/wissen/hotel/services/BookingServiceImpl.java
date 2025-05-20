package com.wissen.hotel.services;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.enums.BookingStatus;
import com.wissen.hotel.exceptions.BadRequestException;
import com.wissen.hotel.exceptions.ResourceNotFoundException;
import com.wissen.hotel.models.*;
import com.wissen.hotel.repositories.*;
import com.wissen.hotel.utils.AuthUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {
        log.info("Creating booking for room {}", request.getRoomId());

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        User user = AuthUtil.getCurrentUser(); // TODO: Replace with real authenticated user ID
    
        validateBookingDates(request.getCheckIn(), request.getCheckOut());

        boolean isAvailable = isRoomAvailable(room.getRoomId(), request.getCheckIn(), request.getCheckOut());
        if (!isAvailable) {
            throw new BadRequestException("Room is not available for the selected dates.");
        }

        long days = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        BigDecimal finalPrice = room.getBasePrice().multiply(BigDecimal.valueOf(days));

        Booking booking = Booking.builder()
                .room(room)
                .user(user)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .guests(request.getGuests())
                .status(BookingStatus.BOOKED)
                .finalPrice(finalPrice)
                .createdAt(LocalDateTime.now())
                .build();

        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponse getBookingById(UUID bookingId) {
        log.info("Fetching booking by ID: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return mapToResponse(booking);
    }

    @Override
    public BookingResponse updateBooking(UUID bookingId, UpdateBookingRequest request) {
        log.info("Updating booking {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        validateBookingDates(request.getCheckIn(), request.getCheckOut());

        boolean isAvailable = isRoomAvailable(booking.getRoom().getRoomId(), request.getCheckIn(), request.getCheckOut(), bookingId);
        if (!isAvailable) {
            throw new BadRequestException("Room is not available for the new dates.");
        }

        booking.setCheckIn(request.getCheckIn());
        booking.setCheckOut(request.getCheckOut());
        booking.setGuests(request.getGuests());

        long days = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        booking.setFinalPrice(booking.getRoom().getBasePrice().multiply(BigDecimal.valueOf(days)));

        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponse cancelBooking(UUID bookingId) {
        log.info("Cancelling booking {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.CANCELLED);
        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    public List<BookingResponse> getAllBookings(String filter) {
        log.info("Fetching all bookings for admin");

        // TODO: Add authentication check to ensure admin access
        return bookingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getBookingsForHotel(UUID hotelId) {
        log.info("Fetching bookings for hotel ID: {}", hotelId);

        // TODO: Add authentication check to ensure hotel owner/admin access
        return bookingRepository.findByRoom_Hotel_HotelId(hotelId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse generateInvoice(UUID bookingId) {
        log.info("Generating invoice for booking {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // You can add detailed invoice logic here
        return mapToResponse(booking);
    }

    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new BadRequestException("Invalid check-in or check-out dates.");
        }
    }

    private boolean isRoomAvailable(UUID roomId, LocalDate checkIn, LocalDate checkOut) {
        return isRoomAvailable(roomId, checkIn, checkOut, null);
    }

    private boolean isRoomAvailable(UUID roomId, LocalDate checkIn, LocalDate checkOut, UUID excludeBookingId) {
        List<Booking> existingBookings = bookingRepository.findByRoom_RoomId(roomId);
    
        for (Booking b : existingBookings) {
            if (excludeBookingId != null && b.getBookingId().equals(excludeBookingId)) continue;
    
            if (b.getStatus() != BookingStatus.CANCELLED &&
                !(checkOut.isBefore(b.getCheckIn()) || checkIn.isAfter(b.getCheckOut()))) {
                return false; // Overlapping booking exists
            }
        }
    
        return true; // No overlapping bookings
    }
    

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUser().getUserId())
                .roomId(booking.getRoom().getRoomId())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .guests(booking.getGuests())
                .finalPrice(booking.getFinalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
