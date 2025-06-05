// package com.wissen.hotel.service;

// import com.wissen.hotel.enums.UserRole;
// import com.wissen.hotel.exception.EmailSendingException;
// import com.wissen.hotel.service.impl.EmailServiceImpl;

// import jakarta.mail.MessagingException;
// import jakarta.mail.internet.MimeMessage;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.*;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.thymeleaf.context.Context;
// import org.thymeleaf.spring6.SpringTemplateEngine;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
// class EmailServiceImplTest {

//     @Mock
//     private JavaMailSender mailSender;

//     @Mock
//     private SpringTemplateEngine templateEngine;

//     @InjectMocks
//     private EmailServiceImpl emailService;

//     @Mock
//     private MimeMessage mimeMessage;

//     @Captor
//     private ArgumentCaptor<String> stringCaptor;

//     @BeforeEach
//     void setUp() {
//         when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
//     }

//     @Test
//     void sendVerificationEmail_shouldSendEmail() {
//         when(templateEngine.process(eq("verification-email"), any(Context.class)))
//                 .thenReturn("<html>Verification</html>");

//         // No exception should be thrown
//         assertDoesNotThrow(() -> emailService.sendVerificationEmail("user@example.com", "token123"));

//         verify(mailSender).send(mimeMessage);
//         verify(templateEngine).process(eq("verification-email"), any(Context.class));
//     }

//     @Test
//     void sendVerificationEmail_shouldThrowEmailSendingException_onTemplateFailure() {
//         when(templateEngine.process(anyString(), any(Context.class)))
//                 .thenThrow(new RuntimeException("Template error"));

//         EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
//                 emailService.sendVerificationEmail("user@example.com", "token123"));
//         assertTrue(ex.getMessage().contains("Failed to send verification email"));
//     }

//     @Test
//     void sendVerificationEmail_shouldThrowEmailSendingException_onMailFailure() {
//         when(templateEngine.process(eq("verification-email"), any(Context.class)))
//                 .thenReturn("<html>Verification</html>");
//         doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(MimeMessage.class));

//         EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
//                 emailService.sendVerificationEmail("user@example.com", "token123"));
//         assertTrue(ex.getMessage().contains("Failed to send HTML email"));
//     }

//     @Test
//     void sendWelcomeEmail_shouldSendHotelOwnerEmail() {
//         when(templateEngine.process(eq("welcome-hotel-owner-email"), any(Context.class)))
//                 .thenReturn("<html>Welcome Owner</html>");

//         assertDoesNotThrow(() -> emailService.sendWelcomeEmail("owner@example.com", "OwnerName", UserRole.HOTEL_OWNER));

//         verify(mailSender).send(mimeMessage);
//         verify(templateEngine).process(eq("welcome-hotel-owner-email"), any(Context.class));
//     }

//     @Test
//     void sendWelcomeEmail_shouldSendGuestEmail() {
//         when(templateEngine.process(eq("welcome-guest-email"), any(Context.class)))
//                 .thenReturn("<html>Welcome Guest</html>");

//         assertDoesNotThrow(() -> emailService.sendWelcomeEmail("guest@example.com", "GuestName", UserRole.CUSTOMER));

//         verify(mailSender).send(mimeMessage);
//         verify(templateEngine).process(eq("welcome-guest-email"), any(Context.class));
//     }

//     @Test
//     void sendWelcomeEmail_shouldThrowEmailSendingException_onError() {
//         when(templateEngine.process(anyString(), any(Context.class)))
//                 .thenThrow(new RuntimeException("Template error"));

//         EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
//                 emailService.sendWelcomeEmail("user@example.com", "User", UserRole.CUSTOMER));
//         assertTrue(ex.getMessage().contains("Failed to send welcome email"));
//     }

//     @Test
//     void sendHotelRegistrationSuccessful_shouldSendEmail() {
//         when(templateEngine.process(eq("hotel-registration-success-email"), any(Context.class)))
//                 .thenReturn("<html>Hotel Registered</html>");

//         assertDoesNotThrow(() -> emailService.sendHotelRegistrationSuccessful("owner@example.com", "HotelName", "OwnerName"));

//         verify(mailSender).send(mimeMessage);
//         verify(templateEngine).process(eq("hotel-registration-success-email"), any(Context.class));
//     }

//     @Test
//     void sendHotelRegistrationSuccessful_shouldThrowEmailSendingException_onError() {
//         when(templateEngine.process(anyString(), any(Context.class)))
//                 .thenThrow(new RuntimeException("Template error"));

//         EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
//                 emailService.sendHotelRegistrationSuccessful("owner@example.com", "HotelName", "OwnerName"));
//         assertTrue(ex.getMessage().contains("Failed to send hotel registration email"));
//     }

//     @Test
//     void sendHotelApprovalNotification_shouldSendEmail() {
//         when(templateEngine.process(eq("hotel-approval-email"), any(Context.class)))
//                 .thenReturn("<html>Hotel Approved</html>");

//         assertDoesNotThrow(() -> emailService.sendHotelApprovalNotification("owner@example.com", "HotelName"));

//         verify(mailSender).send(mimeMessage);
//         verify(templateEngine).process(eq("hotel-approval-email"), any(Context.class));
//     }

//     @Test
//     void sendHotelApprovalNotification_shouldThrowEmailSendingException_onError() {
//         when(templateEngine.process(anyString(), any(Context.class)))
//                 .thenThrow(new RuntimeException("Template error"));

//         EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
//                 emailService.sendHotelApprovalNotification("owner@example.com", "HotelName"));
//         assertTrue(ex.getMessage().contains("Failed to send hotel approval notification"));
//     }

//     @Test
//     void sendAdminHotelRegistrationAlert_shouldSendEmail() {
//         when(templateEngine.process(eq("admin-hotel-alert-email"), any(Context.class)))
//                 .thenReturn("<html>Admin Alert</html>");

//         assertDoesNotThrow(() -> emailService.sendAdminHotelRegistrationAlert("admin@example.com", "HotelName", "owner@example.com"));

//         verify(mailSender).send(mimeMessage);
//         verify(templateEngine).process(eq("admin-hotel-alert-email"), any(Context.class));
//     }

//     @Test
//     void sendAdminHotelRegistrationAlert_shouldThrowEmailSendingException_onError() {
//         when(templateEngine.process(anyString(), any(Context.class)))
//                 .thenThrow(new RuntimeException("Template error"));

//         EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
//                 emailService.sendAdminHotelRegistrationAlert("admin@example.com", "HotelName", "owner@example.com"));
//         assertTrue(ex.getMessage().contains("Failed to send admin hotel registration alert"));
//     }

//     @Test
//     void sendBookingConfirmation_shouldSendEmail() {
//         when(templateEngine.process(eq("booking-confirmation-email"), any(Context.class)))
//                 .thenReturn("<html>Booking Confirmed</html>");

//         assertDoesNotThrow(() -> emailService.sendBookingConfirmation("user@example.com", "BID123", "GuestName"));

//         verify(mailSender).send(mimeMessage);
//         verify(templateEngine).process(eq("booking-confirmation-email"), any(Context.class));
//     }

//     @Test
//     void sendBookingConfirmation_shouldThrowEmailSendingException_onError() {
//         when(templateEngine.process(anyString(), any(Context.class)))
//                 .thenThrow(new RuntimeException("Template error"));

//         EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
//                 emailService.sendBookingConfirmation("user@example.com", "BID123", "GuestName"));
//         assertTrue(ex.getMessage().contains("Failed to send booking confirmation email"));
//     }

//     @Test
//     void sendBookingCancellation_shouldSendEmail() {
//         when(templateEngine.process(eq("booking-cancellation-email"), any(Context.class)))
//                 .thenReturn("<html>Booking Cancelled</html>");

//         assertDoesNotThrow(() -> emailService.sendBookingCancellation("user@example.com", "BID123", "GuestName"));

//         verify(mailSender).send(mimeMessage);
//         verify(templateEngine).process(eq("booking-cancellation-email"), any(Context.class));
//     }

//     @Test
//     void sendBookingCancellation_shouldThrowEmailSendingException_onError() {
//         when(templateEngine.process(anyString(), any(Context.class)))
//                 .thenThrow(new RuntimeException("Template error"));

//         EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
//                 emailService.sendBookingCancellation("user@example.com", "BID123", "GuestName"));
//         assertTrue(ex.getMessage().contains("Failed to send booking cancellation email"));
//     }

//     @Test
//     void sendBookingApprovalToHotelOwner_shouldSendEmail() {
//         when(templateEngine.process(eq("booking-approval-hotel-owner-email"), any(Context.class)))
//                 .thenReturn("<html>Booking Approved</html>");

//         assertDoesNotThrow(() -> emailService.sendBookingApprovalToHotelOwner("owner@example.com", "BID123", "GuestName"));

//         verify(mailSender).send(mimeMessage);
//         verify(templateEngine).process(eq("booking-approval-hotel-owner-email"), any(Context.class));
//     }

//     @Test
//     void sendBookingApprovalToHotelOwner_shouldThrowEmailSendingException_onError() {
//         when(templateEngine.process(anyString(), any(Context.class)))
//                 .thenThrow(new RuntimeException("Template error"));

//         EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
//                 emailService.sendBookingApprovalToHotelOwner("owner@example.com", "BID123", "GuestName"));
//         assertTrue(ex.getMessage().contains("Failed to send booking approval email to hotel owner"));
//     }

//     @Test
//     void sendBookingSuccessToUser_shouldSendEmail() {
//         when(templateEngine.process(eq("booking-success-email"), any(Context.class)))
//                 .thenReturn("<html>Booking Success</html>");

//         assertDoesNotThrow(() -> emailService.sendBookingSuccessToUser("user@example.com", "BID123", "GuestName"));

//         verify(mailSender).send(mimeMessage);
//         verify(templateEngine).process(eq("booking-success-email"), any(Context.class));
//     }

//     @Test
//     void sendBookingSuccessToUser_shouldThrowEmailSendingException_onError() {
//         when(templateEngine.process(anyString(), any(Context.class)))
//                 .thenThrow(new RuntimeException("Template error"));

//         EmailSendingException ex = assertThrows(EmailSendingException.class, () ->
//                 emailService.sendBookingSuccessToUser("user@example.com", "BID123", "GuestName"));
//         assertTrue(ex.getMessage().contains("Failed to send booking success email"));
//     }
// }
