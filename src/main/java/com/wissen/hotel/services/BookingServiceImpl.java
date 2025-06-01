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

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final RoomAvailabilityService roomAvailabilityService;

    private static final String BOOKING_NOT_FOUND = "Booking not found";

    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        User user = AuthUtil.getCurrentUser();

        validateBookingDates(request.getCheckIn(), request.getCheckOut());

        boolean isAvailable = isRoomAvailable(room.getRoomId(), request.getCheckIn(), request.getCheckOut());
        if (!isAvailable) {
            throw new BadRequestException("Room is not available for the selected dates.");
        }

        long days = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        BigDecimal finalPrice = room.getBasePrice().multiply(BigDecimal.valueOf(days)).multiply(BigDecimal.valueOf(request.getRoomsBooked()));

        Booking booking = Booking.builder()
                .room(room)
                .user(user)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .guests(request.getGuests())
                .roomsBooked(request.getRoomsBooked())
                .status(BookingStatus.PENDING)
                .finalPrice(finalPrice)
                .createdAt(LocalDateTime.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        approveBooking(booking.getBookingId());
        return mapToResponse(savedBooking);
    }

    @Override
    public BookingResponse getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(BOOKING_NOT_FOUND));
        return mapToResponse(booking);
    }

    @Override
    public BookingResponse updateBooking(UUID bookingId, UpdateBookingRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(BOOKING_NOT_FOUND));

        validateBookingDates(request.getCheckIn(), request.getCheckOut());

        boolean isAvailable = isRoomAvailable(booking.getRoom().getRoomId(), request.getCheckIn(), request.getCheckOut(), bookingId);
        if (!isAvailable) {
            throw new BadRequestException("Room is not available for the new dates.");
        }

        LocalDate oldCurrent = booking.getCheckIn();
        while (oldCurrent.isBefore(booking.getCheckOut())) {
            UpdateInventoryRequest restoreRequest = new UpdateInventoryRequest();
            restoreRequest.setDate(oldCurrent);
            restoreRequest.setRoomsToBook(-booking.getRoomsBooked());
            roomAvailabilityService.updateInventory(booking.getRoom().getRoomId(), restoreRequest);
            oldCurrent = oldCurrent.plusDays(1);
        }

        booking.setCheckIn(request.getCheckIn());
        booking.setCheckOut(request.getCheckOut());
        booking.setGuests(request.getGuests());
        booking.setRoomsBooked(request.getRoomsBooked());

        long days = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        booking.setFinalPrice(booking.getRoom().getBasePrice().multiply(BigDecimal.valueOf(days)).multiply(BigDecimal.valueOf(request.getRoomsBooked())));

        LocalDate newCurrent = booking.getCheckIn();
        while (newCurrent.isBefore(booking.getCheckOut())) {
            UpdateInventoryRequest deductRequest = new UpdateInventoryRequest();
            deductRequest.setDate(newCurrent);
            deductRequest.setRoomsToBook(booking.getRoomsBooked());
            roomAvailabilityService.updateInventory(booking.getRoom().getRoomId(), deductRequest);
            newCurrent = newCurrent.plusDays(1);
        }
        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponse approveBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException(BOOKING_NOT_FOUND));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING bookings can be approved.");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        LocalDate current = booking.getCheckIn();
        while (current.isBefore(booking.getCheckOut())) {
            UpdateInventoryRequest inventoryRequest = new UpdateInventoryRequest();
            inventoryRequest.setDate(current);
            inventoryRequest.setRoomsToBook(booking.getRoomsBooked());
            roomAvailabilityService.updateInventory(booking.getRoom().getRoomId(), inventoryRequest);
            current = current.plusDays(1);
        }

        return mapToResponse(booking);
    }

    @Override
    public BookingResponse cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(BOOKING_NOT_FOUND));

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        LocalDate current = booking.getCheckIn();
        while (current.isBefore(booking.getCheckOut())) {
            UpdateInventoryRequest inventoryRequest = new UpdateInventoryRequest();
            inventoryRequest.setDate(current);
            inventoryRequest.setRoomsToBook(-booking.getRoomsBooked());
            roomAvailabilityService.updateInventory(booking.getRoom().getRoomId(), inventoryRequest);
            current = current.plusDays(1);
        }

        return mapToResponse(booking);
    }

    @Override
    public List<BookingResponse> getAllBookings(String filter) {
        return bookingRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<BookingResponse> getBookingsForHotel(UUID hotelId) {
        return bookingRepository.findByRoom_Hotel_HotelId(hotelId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BookingResponse generateInvoice(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(BOOKING_NOT_FOUND));
        return mapToResponse(booking);
    }

    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new BadRequestException("Invalid check-in or check-out dates.");
        }
    }

    @Override
    public boolean isRoomAvailable(UUID roomId, LocalDate checkIn, LocalDate checkOut) {
        return isRoomAvailable(roomId, checkIn, checkOut, null);
    }

    public boolean isRoomAvailable(UUID roomId, LocalDate checkIn, LocalDate checkOut, UUID excludeBookingId) {
        List<Booking> existingBookings = bookingRepository.findByRoom_RoomId(roomId);

        for (Booking booking : existingBookings) {
            if (excludeBookingId != null && booking.getBookingId().equals(excludeBookingId)) continue;
            if (booking.getStatus() == BookingStatus.CANCELLED) continue;
            boolean overlaps = !(checkOut.isBefore(booking.getCheckIn()) || checkOut.equals(booking.getCheckIn())
                    || checkIn.isAfter(booking.getCheckOut()) || checkIn.equals(booking.getCheckOut()));
            if (overlaps) return false;
        }
        return roomAvailabilityService.isRoomAvailableForRange(roomId, checkIn, checkOut);
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUser().getUserId())
                .roomId(booking.getRoom().getRoomId())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .guests(booking.getGuests())
                .roomsBooked(booking.getRoomsBooked())
                .finalPrice(booking.getFinalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
