package com.wissen.hotel.service;

import com.wissen.hotel.enums.UserRole;
import com.wissen.hotel.exception.EmailSendingException;
import com.wissen.hotel.service.impl.EmailServiceImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


class EmailServiceImplTest {

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

    @Test
    void testSendEmail_Success() {
        String to = "test@example.com";
        String from = "noreply@example.com";
        String subject = "Test Subject";
        String text = "Test Content";

        // Arrange: Mock mailSender to do nothing (or verify it was called)
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendEmail(to, from, subject, text);

        // Assert
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmail_Exception() {
        String to = "test@example.com";
        String from = "noreply@example.com";
        String subject = "Test Subject";
        String text = "Test Content";

        // Arrange: Make mailSender throw an exception
        doThrow(new RuntimeException("Simulated error"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> emailService.sendEmail(to, from, subject, text));
        assertEquals("Failed to send email", ex.getMessage());
    }

    @Test
    void testSendEmail_InvalidRecipient() {
        doThrow(new MailSendException("Invalid address")).when(mailSender).send(any(SimpleMailMessage.class));
        
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> emailService.sendEmail("invalid@", "valid@example.com", "Test", "Content"));
        
        assertEquals("Failed to send email", ex.getMessage());
        assertTrue(ex.getCause() instanceof MailSendException);
    }

    @Test
    void testSendEmail_NullFromAddress() {
        doThrow(new IllegalArgumentException("From address cannot be null"))
            .when(mailSender).send(any(SimpleMailMessage.class));
        
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> emailService.sendEmail("valid@example.com", null, "Subject", "Text"));
        
        assertEquals("Failed to send email", ex.getMessage());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void testSendEmail_AuthenticationFailure() {
        doThrow(new MailAuthenticationException("Invalid credentials"))
            .when(mailSender).send(any(SimpleMailMessage.class));
        
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> emailService.sendEmail("test@example.com", "noreply@example.com", "Test", "Content"));
        
        assertEquals("Failed to send email", ex.getMessage());
        assertTrue(ex.getCause() instanceof MailAuthenticationException);
    }

    @Test
    void testSendEmail_EmptySubject() {
        doThrow(new MailSendException("Subject cannot be empty"))
            .when(mailSender).send(any(SimpleMailMessage.class));
        
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> emailService.sendEmail("test@example.com", "noreply@example.com", "", "Content"));
        
        assertEquals("Failed to send email", ex.getMessage());
        assertTrue(ex.getCause() instanceof MailSendException);
    }

    @Test
    void testSendEmail_LongContent() {
        String longText = "a".repeat(10_000);
        doThrow(new MailSendException("Content too large"))
            .when(mailSender).send(any(SimpleMailMessage.class));
        
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> emailService.sendEmail("test@example.com", "noreply@example.com", "Subject", longText));
        
        assertEquals("Failed to send email", ex.getMessage());
        assertTrue(ex.getCause() instanceof MailSendException);
    }
    // Additional tests for EmailServiceImpl to increase coverage

    @Test
    void testSendWelcomeEmail_Guest() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("welcome-guest-email"), any(Context.class))).thenReturn("<html>content</html>");

        emailService.sendWelcomeEmail("guest@example.com", "Guest", UserRole.CUSTOMER);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendWelcomeEmail_Exception() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
                emailService.sendWelcomeEmail("user@example.com", "User", UserRole.CUSTOMER));
        assertEquals("Failed to send welcome email", ex.getMessage());
    }

    @Test
    void testSendHotelRegistrationSuccessful_Exception() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
                emailService.sendHotelRegistrationSuccessful("user@example.com", "Hotel", "Owner"));
        assertEquals("Failed to send hotel registration email", ex.getMessage());
    }

    @Test
    void testSendHotelApprovalNotification_Exception() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
                emailService.sendHotelApprovalNotification("user@example.com", "Hotel"));
        assertEquals("Failed to send hotel approval notification", ex.getMessage());
    }

    @Test
    void testSendAdminHotelRegistrationAlert_Exception() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
                emailService.sendAdminHotelRegistrationAlert("admin@example.com", "Hotel", "owner@example.com"));
        assertEquals("Failed to send admin hotel registration alert", ex.getMessage());
    }

    @Test
    void testSendBookingConfirmation_Exception() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
                emailService.sendBookingConfirmation("guest@example.com", "BID", "Guest"));
        assertEquals("Failed to send booking confirmation email", ex.getMessage());
    }

    @Test
    void testSendBookingCancellation_Exception() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
                emailService.sendBookingCancellation("guest@example.com", "BID", "Guest"));
        assertEquals("Failed to send booking cancellation email", ex.getMessage());
    }

    @Test
    void testSendBookingApprovalToHotelOwner_Exception() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
                emailService.sendBookingApprovalToHotelOwner("owner@example.com", "BID", "Guest"));
        assertEquals("Failed to send booking approval email to hotel owner", ex.getMessage());
    }

    @Test
    void testSendBookingSuccessToUser_Exception() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenThrow(new RuntimeException("Template error"));

        EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
                emailService.sendBookingSuccessToUser("guest@example.com", "BID", "Guest"));
        assertEquals("Failed to send booking success email", ex.getMessage());
    }

    @Test
    void testSendHtmlEmail_MessagingException() throws Exception {
        // Use reflection to call private sendHtmlEmail for coverage
        EmailServiceImpl impl = new EmailServiceImpl(mailSender, templateEngine);
        MimeMessage msg = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(msg);
        doThrow(new MessagingException("fail")).when(mailSender).send(any(MimeMessage.class));

        // Use reflection to access private method
        java.lang.reflect.Method m = EmailServiceImpl.class.getDeclaredMethod("sendHtmlEmail", String.class, String.class, String.class);
        m.setAccessible(true);

        EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
                m.invoke(impl, "to@example.com", "Subject", "<html>content</html>"));
        assertTrue(ex.getCause() instanceof MessagingException);
    }
}
