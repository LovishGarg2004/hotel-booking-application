package com.wissen.hotel.service;

import com.wissen.hotel.enums.UserRole;
import com.wissen.hotel.exception.EmailSendingException;
import com.wissen.hotel.service.impl.EmailServiceImpl;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailServiceImpl(mailSender, templateEngine);
    }

    @Test
    void testSendVerificationEmail() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("verification-email"), any(Context.class))).thenReturn("<html>content</html>");

        emailService.sendVerificationEmail("test@example.com", "dummy-token");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendWelcomeEmail_HotelOwner() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("welcome-hotel-owner-email"), any(Context.class))).thenReturn("<html>content</html>");

        emailService.sendWelcomeEmail("owner@example.com", "Owner", UserRole.HOTEL_OWNER);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendHotelRegistrationSuccessful() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("hotel-registration-success-email"), any(Context.class))).thenReturn("<html>content</html>");

        emailService.sendHotelRegistrationSuccessful("user@example.com", "Hotel Lux", "Owner Name");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendHotelApprovalNotification() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("hotel-approval-email"), any(Context.class))).thenReturn("<html>content</html>");

        emailService.sendHotelApprovalNotification("user@example.com", "Hotel Lux");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendAdminHotelRegistrationAlert() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("admin-hotel-alert-email"), any(Context.class))).thenReturn("<html>content</html>");

        emailService.sendAdminHotelRegistrationAlert("admin@example.com", "Hotel Lux", "owner@example.com");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendBookingConfirmation() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("booking-confirmation-email"), any(Context.class))).thenReturn("<html>content</html>");

        emailService.sendBookingConfirmation("guest@example.com", "BID123", "Guest Name");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendBookingCancellation() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("booking-cancellation-email"), any(Context.class))).thenReturn("<html>content</html>");

        emailService.sendBookingCancellation("guest@example.com", "BID123", "Guest Name");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendBookingApprovalToHotelOwner() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("booking-approval-hotel-owner-email"), any(Context.class))).thenReturn("<html>content</html>");

        emailService.sendBookingApprovalToHotelOwner("owner@example.com", "BID123", "Guest Name");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendBookingSuccessToUser() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("booking-success-email"), any(Context.class))).thenReturn("<html>content</html>");

        emailService.sendBookingSuccessToUser("guest@example.com", "BID123", "Guest Name");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmailThrowsException() {
        doThrow(RuntimeException.class).when(mailSender).send(any(MimeMessage.class));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>content</html>");

        assertThrows(EmailSendingException.class, () ->
                emailService.sendVerificationEmail("test@example.com", "token"));
    }
}
