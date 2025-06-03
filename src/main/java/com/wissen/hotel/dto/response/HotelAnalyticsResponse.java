package com.wissen.hotel.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelAnalyticsResponse {

    private Overview overview;
    private List<RecentBooking> recentBookings;
    private List<RoomPerformance> roomPerformance;
    private BookingStatusDistribution bookingStatus;
    private List<MonthlyRevenue> monthlyRevenue;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Overview {
        private long totalBookings;
        private BigDecimal totalRevenue;
        private double averageRating;
        private double occupancyRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentBooking {
        private String date;
        private long bookings;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomPerformance {
        private String roomType;
        private long bookings;
        private BigDecimal revenue;
        private double occupancyRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingStatusDistribution {
        private long confirmed;
        private long pending;
        private long cancelled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenue {
        private String month;
        private BigDecimal revenue;
    }
}
