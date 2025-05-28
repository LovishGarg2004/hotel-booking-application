package com.wissen.hotel.models;

import com.wissen.hotel.enums.ImageType;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID imageId;

    @Enumerated(EnumType.STRING)
    private ImageType type;

    private UUID referenceId;

    private String imageUrl;

    private LocalDateTime uploadedAt;
}
