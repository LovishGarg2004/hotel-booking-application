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
            String link = "http://localhost:8081/api/auth/verify?token=" + token;
            
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
}

