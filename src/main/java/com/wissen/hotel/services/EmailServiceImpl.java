package com.wissen.hotel.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.wissen.hotel.enums.UserRole;

import org.hibernate.usertype.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailServiceImpl implements EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        try {
            String link = "http://localhost:8081/api/auth/verify/" + token;
            
            logger.info("Sending verification email to: {}", to);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@hotelbooking.com");
            message.setTo(to);
            message.setSubject("Verify your email");
            message.setText("Click the following link to verify your email: " + link);
            
            mailSender.send(message);
            logger.info("Verification email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Error sending verification email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
    
    @Override
    public void sendWelcomeEmail(String to, String name, UserRole userRole) {
        String subject;
        String content;
        if (userRole == UserRole.HOTEL_OWNER) {
            subject = "Welcome to Stay Wise ! Hotel Owner!";
            content = String.format(
                "Dear %s,\n\n" +
                "Welcome to Hotel Booking Platform! We're excited to have you on board as a hotel owner.\n" +
                "Thank you for registering. You can now start listing your properties and manage your bookings.\n\n" +
                "If you have any questions or need assistance, feel free to contact our support team.\n\n" +
                "Best regards,\n" +
                "The Hotel Booking Team",
                name
            );
        } else {
            subject = "Welcome to Stay Wise!";
            content = String.format(
                "Dear %s,\n\n" +
                "Welcome to Hotel Booking Platform! We're excited to have you on board.\n" +
                "Thank you for registering with us. You can now browse and book hotels.\n\n" +
                "If you have any questions or need assistance, feel free to contact our support team.\n\n" +
                "Best regards,\n" +
                "The Hotel Booking Team",
                name
            );
        }
        sendEmail(to, "welcome@hotelbooking.com", subject, content);
    }


    @Override
    public void sendHotelRegistrationSuccessful(String to, String hotelName, String ownerName) {
        String content = String.format(
            "Dear %s,\n\nYour hotel '%s' has been registered successfully. ...",
            ownerName, hotelName);
        sendEmail(to, "registration@hotelbooking.com", "Hotel Registration Successful", content);
    }

    @Override
    public void sendHotelApprovalNotification(String to, String hotelName) {
        String content = String.format(
            "Dear Hotel Owner,\n\nYour hotel '%s' has been approved by the admin. ...",
            hotelName);
        sendEmail(to, "approval@hotelbooking.com", "Hotel Approval Notification", content);
    }

    @Override
    public void sendAdminHotelRegistrationAlert(String adminEmail, String hotelName, String ownerEmail) {
        String content = String.format(
            "Dear Admin,\n\nA new hotel '%s' has been registered by '%s'. ...",
            hotelName, ownerEmail);
        sendEmail(adminEmail, "alerts@hotelbooking.com", "New Hotel Registration Alert", content);
    }

    @Override
    public void sendBookingConfirmation(String to, String bookingId, String guestName) {
        String content = String.format(
            "Dear %s,\n\nYour booking (ID: %s) has been confirmed. ...",
            guestName, bookingId);
        sendEmail(to, "bookings@hotelbooking.com", "Booking Confirmation", content);
    }

    @Override
    public void sendBookingCancellation(String to, String bookingId, String guestName) {
        String content = String.format(
            "Dear %s,\n\nYour booking (ID: %s) has been cancelled. ...",
            guestName, bookingId);
        sendEmail(to, "bookings@hotelbooking.com", "Booking Cancellation", content);
    }

    @Override
    public void sendBookingApprovalToHotelOwner(String to, String bookingId, String guestName) {
        String content = String.format(
            "Dear Hotel Owner,\n\nA new booking (ID: %s) from '%s' has been approved. ...",
            bookingId, guestName);
        sendEmail(to, "bookings@hotelbooking.com", "New Booking Approved", content);
    }

    @Override
    public void sendBookingSuccessToUser(String to, String bookingId, String guestName) {
        String content = String.format(
            "Dear %s,\n\nYour booking (ID: %s) was successful. ...",
            guestName, bookingId);
        sendEmail(to, "bookings@hotelbooking.com", "Booking Successful", content);
    }

    private void sendEmail(String to, String from, String subject, String text) {
        try {
            logger.info("Sending email to: {}", to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Error sending email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}

