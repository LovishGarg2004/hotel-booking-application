package com.wissen.hotel.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID hotelId;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    private String name;

    @Lob
    @Column(name = "description", length = 10000)
    private String description;

    @Lob
    private String address;

    private String city;
    private String state;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime createdAt;
    private boolean isApproved;
    

}
