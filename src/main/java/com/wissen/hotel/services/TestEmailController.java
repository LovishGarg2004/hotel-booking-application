package com.wissen.hotel.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-email")
public class TestEmailController {

    private final EmailServiceImpl emailSender;

    @Autowired
    public TestEmailController(EmailServiceImpl emailSender) {
        this.emailSender = emailSender;
    }

    @PostMapping
    public ResponseEntity<String> testSendEmailPost(@RequestBody EmailRequest emailRequest) {
        return sendVerificationEmail(emailRequest.getTo());
    }
    
    @GetMapping
    public ResponseEntity<String> testSendEmailGet(@RequestParam String to) {
        return sendVerificationEmail(to);
    }
    
    private ResponseEntity<String> sendVerificationEmail(String to) {
        try {
            emailSender.sendVerificationEmail(to, "test-verification-token");
            return ResponseEntity.ok("Verification email sent to " + to);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
        }
    }
    
    // Inner class for the request body
    public static class EmailRequest {
        private String to;
        
        // Getters and setters
        public String getTo() {
            return to;
        }
        
        public void setTo(String to) {
            this.to = to;
        }
    }
}

