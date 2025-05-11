package com.wissen.hotel.models;

import com.wissen.hotel.enums.PaymentStatus;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID paymentId;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}
