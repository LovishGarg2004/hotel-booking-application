package com.wissen.hotel.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Amenity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID amenityId;

    private String name;
    private String description;
    private String imageUrl;
}
