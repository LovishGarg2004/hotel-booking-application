package com.wissen.hotel.services;

import com.wissen.hotel.exceptions.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.wissen.hotel.enums.UserRole;

import org.hibernate.usertype.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;


@Service
public class EmailServiceImpl implements EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailServiceImpl(JavaMailSender mailSender,SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine=templateEngine;
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            logger.info("Sending HTML email to: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("jashitgoyal@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates this is HTML content

            mailSender.send(message);
            logger.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Error sending HTML email to {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send HTML email", e);
        }
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        try {
            String verificationLink = "http://localhost:8081/api/auth/verify/" + token;

            logger.info("Sending verification email to: {}", to);

            Context context = new Context();
            context.setVariable("verificationLink", verificationLink);
            context.setVariable("email", to);

            String htmlContent = templateEngine.process("verification-email", context);
            sendHtmlEmail(to, "Verify Your Stay Wise Account", htmlContent);

            logger.info("Verification email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Error sending verification email to {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send verification email", e);
        }
    }

    @Override
    public void sendWelcomeEmail(String to, String name, UserRole userRole) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("userRole", userRole.toString());
            context.setVariable("email", to);

            String templateName = (userRole == UserRole.HOTEL_OWNER) ?
                    "welcome-hotel-owner-email" : "welcome-guest-email";

            String htmlContent = templateEngine.process(templateName, context);
            String subject = (userRole == UserRole.HOTEL_OWNER) ?
                    "Welcome to Stay Wise! Start Your Hotel Journey" :
                    "Welcome to Stay Wise! Your Gateway to Perfect Stays";

            sendHtmlEmail(to, subject, htmlContent);

            logger.info("Welcome email sent successfully to {} with role {}", to, userRole);
        } catch (Exception e) {
            logger.error("Error sending welcome email to {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send welcome email", e);
        }
    }


    @Override
    public void sendHotelRegistrationSuccessful(String to, String hotelName, String ownerName) {
        try {
            Context context = new Context();
            context.setVariable("hotelName", hotelName);
            context.setVariable("ownerName", ownerName);

            String htmlContent = templateEngine.process("hotel-registration-success-email", context);
            sendHtmlEmail(to, "Hotel Registration Successful", htmlContent);

            logger.info("Hotel registration success email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Error sending hotel registration email to {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send hotel registration email", e);
        }

    }

    @Override
    public void sendHotelApprovalNotification(String to, String hotelName) {
        try {
            Context context = new Context();
            context.setVariable("hotelName", hotelName);

            String htmlContent = templateEngine.process("hotel-approval-email", context);
            sendHtmlEmail(to, "Your Hotel Has Been Approved!", htmlContent);

            logger.info("Hotel approval notification sent to: {}", to);
        } catch (Exception e) {
            logger.error("Error sending hotel approval notification to {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send hotel approval notification", e);
        }

    }

    @Override
    public void sendAdminHotelRegistrationAlert(String adminEmail, String hotelName, String ownerEmail) {
        try {
            Context context = new Context();
            context.setVariable("hotelName", hotelName);
            context.setVariable("ownerEmail", ownerEmail);
            context.setVariable("registrationDate", LocalDateTime.now());

            String htmlContent = templateEngine.process("admin-hotel-alert-email", context);
            sendHtmlEmail(adminEmail, "New Hotel Registration Alert", htmlContent);

            logger.info("Admin alert for hotel registration sent to: {}", adminEmail);
        } catch (Exception e) {
            logger.error("Error sending admin hotel registration alert to {}: {}", adminEmail, e.getMessage(), e);
            throw new EmailSendingException("Failed to send admin hotel registration alert", e);
        }
    }

    @Override
    public void sendBookingConfirmation(String to, String bookingId, String guestName) {try {
        Context context = new Context();
        context.setVariable("bookingId", bookingId);
        context.setVariable("guestName", guestName);

        String htmlContent = templateEngine.process("booking-confirmation-email", context);
        sendHtmlEmail(to, "Your Booking is Confirmed!", htmlContent);

        logger.info("Booking confirmation email sent successfully to: {}", to);
    } catch (Exception e) {
        logger.error("Error sending booking confirmation email to {}: {}", to, e.getMessage(), e);
        throw new EmailSendingException("Failed to send booking confirmation email", e);
    }

    }

    @Override
    public void sendBookingCancellation(String to, String bookingId, String guestName) {
        try {
            Context context = new Context();
            context.setVariable("bookingId", bookingId);
            context.setVariable("guestName", guestName);

            String htmlContent = templateEngine.process("booking-cancellation-email", context);
            sendHtmlEmail(to, "Booking Cancellation Confirmation", htmlContent);

            logger.info("Booking cancellation email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Error sending booking cancellation email to {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send booking cancellation email", e);
        }
    }

    @Override
    public void sendBookingApprovalToHotelOwner(String to, String bookingId, String guestName) {

        try {
            Context context = new Context();
            context.setVariable("bookingId", bookingId);
            context.setVariable("guestName", guestName);

            String htmlContent = templateEngine.process("booking-approval-hotel-owner-email", context);
            sendHtmlEmail(to, "New Booking Approved", htmlContent);

            logger.info("Booking approval email sent successfully to hotel owner: {}", to);
        } catch (Exception e) {
            logger.error("Error sending booking approval email to hotel owner {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send booking approval email to hotel owner", e);
        }

    }

    @Override
    public void sendBookingSuccessToUser(String to, String bookingId, String guestName) {
        try {
            Context context = new Context();
            context.setVariable("bookingId", bookingId);
            context.setVariable("guestName", guestName);

            String htmlContent = templateEngine.process("booking-success-email", context);
            sendHtmlEmail(to, "Booking Confirmation", htmlContent);

            logger.info("Booking success email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Error sending booking success email to {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send booking success email", e);
        }
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

