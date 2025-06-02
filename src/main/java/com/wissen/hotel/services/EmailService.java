package com.wissen.hotel.services;

import com.wissen.hotel.enums.UserRole;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
    void sendWelcomeEmail(String to, String name, UserRole userRole);
    void sendHotelRegistrationSuccessful(String to, String hotelName, String ownerName);
    void sendHotelApprovalNotification(String to, String hotelName);
    void sendAdminHotelRegistrationAlert(String adminEmail, String hotelName, String ownerEmail);
    void sendBookingConfirmation(String to, String bookingId, String guestName);
    void sendBookingCancellation(String to, String bookingId, String guestName);
    void sendBookingApprovalToHotelOwner(String to, String bookingId, String guestName);
    void sendBookingSuccessToUser(String to, String bookingId, String guestName);
}
