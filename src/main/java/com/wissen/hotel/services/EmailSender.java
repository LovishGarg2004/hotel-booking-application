package com.wissen.hotel.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailSender {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);
    
    private final JavaMailSender mailSender;

    @Autowired
    public EmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

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
    
    public void sendWelcomeEmail(String to, String name) {
        try {
            logger.info("Sending welcome email to: {}", to);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("welcome@hotelbooking.com");
            message.setTo(to);
            message.setSubject("Welcome to Hotel Booking Platform!");
            
            String emailContent = String.format(
                "Dear %s,\n\n" +
                "Welcome to Hotel Booking Platform! We're excited to have you on board.\n" +
                "Thank you for registering as a hotel owner. You can now start listing your properties and manage your bookings.\n\n" +
                "If you have any questions or need assistance, feel free to contact our support team.\n\n" +
                "Best regards,\n" +
                "The Hotel Booking Team",
                name
            );
            
            message.setText(emailContent);
            
            mailSender.send(message);
            logger.info("Welcome email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Error sending welcome email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }
}

