package com.wissen.hotel.models;

import com.wissen.hotel.enums.PricingRuleType;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRule {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID ruleId;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Enumerated(EnumType.STRING)
    private PricingRuleType ruleType;

    @Lob
    private String value;
    private LocalDate startDate;
    private LocalDate endDate;
}
