package com.wissen.hotel.services;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.enums.BookingStatus;
import com.wissen.hotel.models.Room;
import com.wissen.hotel.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final BookingService bookingService;
    private final RoomRepository roomRepository;
    private final HotelService hotelService;

    @Override
    public HotelAnalyticsResponse getHotelAnalytics(UUID hotelId) {
        List<BookingResponse> bookings = bookingService.getBookingsForHotel(hotelId);
        List<Room> rooms = roomRepository.findAllByHotel_HotelId(hotelId);
        double averageRating = hotelService.getAverageRating(hotelId);

        return HotelAnalyticsResponse.builder()
            .overview(createOverview(bookings, rooms, averageRating))
            .recentBookings(createRecentBookings(bookings))
            .roomPerformance(createRoomPerformance(bookings, rooms))
            .bookingStatus(createBookingStatus(bookings))
            .monthlyRevenue(createMonthlyRevenue(bookings))
            .build();
    }

    private HotelAnalyticsResponse.Overview createOverview(List<BookingResponse> bookings,
                                                         List<Room> rooms, double averageRating) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(29);

        long totalBookings = bookings.size();
        BigDecimal totalRevenue = getConfirmedBookingsRevenue(bookings);
        double occupancyRate = calculateOccupancyRate(bookings, rooms, thirtyDaysAgo, today);

        return HotelAnalyticsResponse.Overview.builder()
            .totalBookings(totalBookings)
            .totalRevenue(totalRevenue)
            .averageRating(averageRating)
            .occupancyRate(occupancyRate)
            .build();
    }

    private List<HotelAnalyticsResponse.RecentBooking> createRecentBookings(List<BookingResponse> bookings) {
        LocalDate today = LocalDate.now();
        List<HotelAnalyticsResponse.RecentBooking> recentBookings = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long dailyBookings = countDailyBookings(bookings, date);
            BigDecimal dailyRevenue = calculateDailyRevenue(bookings, date);

            recentBookings.add(HotelAnalyticsResponse.RecentBooking.builder()
                .date(date.toString())
                .bookings(dailyBookings)
                .revenue(dailyRevenue)
                .build());
        }
        return recentBookings;
    }

    private List<HotelAnalyticsResponse.RoomPerformance> createRoomPerformance(List<BookingResponse> bookings,
                                                                             List<Room> rooms) {
        Map<String, List<BookingResponse>> bookingsByRoomType = groupBookingsByRoomType(rooms, bookings);

        return bookingsByRoomType.entrySet().stream()
            .map(entry -> buildRoomPerformance(entry.getKey(), entry.getValue(), rooms.stream().mapToInt(Room::getTotalRooms).sum()))
            .toList();
    }

    private HotelAnalyticsResponse.BookingStatusDistribution createBookingStatus(List<BookingResponse> bookings) {
        long confirmed = countByStatus(bookings, BookingStatus.CONFIRMED);
        long pending = countByStatus(bookings, BookingStatus.PENDING);
        long cancelled = countByStatus(bookings, BookingStatus.CANCELLED);

        return HotelAnalyticsResponse.BookingStatusDistribution.builder()
            .confirmed(confirmed)
            .pending(pending)
            .cancelled(cancelled)
            .build();
    }

    private List<HotelAnalyticsResponse.MonthlyRevenue> createMonthlyRevenue(List<BookingResponse> bookings) {
        LocalDate today = LocalDate.now();
        List<HotelAnalyticsResponse.MonthlyRevenue> monthlyRevenue = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i).withDayOfMonth(1);
            String monthStr = month.getYear() + "-" + String.format("%02d", month.getMonthValue());
            BigDecimal revenue = calculateMonthlyRevenue(bookings, month);

            monthlyRevenue.add(HotelAnalyticsResponse.MonthlyRevenue.builder()
                .month(monthStr)
                .revenue(revenue)
                .build());
        }
        return monthlyRevenue;
    }

    // Helper methods
    private BigDecimal getConfirmedBookingsRevenue(List<BookingResponse> bookings) {
        return bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
            .map(BookingResponse::getFinalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double calculateOccupancyRate(List<BookingResponse> bookings, List<Room> rooms,
                                        LocalDate start, LocalDate end) {
        int totalRooms = rooms.stream().mapToInt(Room::getTotalRooms).sum();
        if (totalRooms == 0) return 0.0;
        long totalNights = ChronoUnit.DAYS.between(start, end.plusDays(1));
        long bookedNights = bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
            .flatMap(b -> getBookingNights(b, start, end))
            .count();

        return (double) bookedNights / (totalRooms * totalNights);
    }

    private Stream<LocalDate> getBookingNights(BookingResponse booking, LocalDate start, LocalDate end) {
        return booking.getCheckIn().datesUntil(booking.getCheckOut())
            .filter(date -> !date.isBefore(start) && !date.isAfter(end));
    }

    private long countDailyBookings(List<BookingResponse> bookings, LocalDate date) {
        return bookings.stream()
            .filter(b -> b.getCreatedAt().toLocalDate().equals(date))
            .count();
    }

    private BigDecimal calculateDailyRevenue(List<BookingResponse> bookings, LocalDate date) {
        return bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
            .filter(b -> b.getCreatedAt().toLocalDate().equals(date))
            .map(BookingResponse::getFinalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, List<BookingResponse>> groupBookingsByRoomType(List<Room> rooms,
                                                                      List<BookingResponse> bookings) {
        Map<UUID, String> roomIdToType = rooms.stream()
            .collect(Collectors.toMap(
                Room::getRoomId,
                room -> room.getRoomType().name()
            ));

        return bookings.stream()
            .collect(Collectors.groupingBy(
                b -> roomIdToType.getOrDefault(b.getRoomId(), "Unknown")
            ));
    }

    private HotelAnalyticsResponse.RoomPerformance buildRoomPerformance(String roomType,
                                                                       List<BookingResponse> bookings,
                                                                       int totalRooms) {
        long bookingsCount = bookings.size();
        BigDecimal revenue = bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
            .map(BookingResponse::getFinalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        double occupancyRate = bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
            .mapToInt(b -> b.getRoomsBooked() * (int) ChronoUnit.DAYS.between(b.getCheckIn(), b.getCheckOut()))
            .sum() / (double) totalRooms;

        return HotelAnalyticsResponse.RoomPerformance.builder()
            .roomType(roomType)
            .bookings(bookingsCount)
            .revenue(revenue)
            .occupancyRate(occupancyRate)
            .build();
    }

    private long countByStatus(List<BookingResponse> bookings, BookingStatus status) {
        return bookings.stream()
            .filter(b -> b.getStatus() == status)
            .count();
    }

    private BigDecimal calculateMonthlyRevenue(List<BookingResponse> bookings, LocalDate month) {
        return bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
            .filter(b -> b.getCheckIn().getMonthValue() == month.getMonthValue() &&
                       b.getCheckIn().getYear() == month.getYear())
            .map(BookingResponse::getFinalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
