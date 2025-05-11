package com.wissen.hotel.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID userId;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private LocalDate dob;

    private String phone;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private LocalDateTime createdAt;
}
