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
@Table(
    name = "room_amenity",
    uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "amenity_id"})
)
public class RoomAmenity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "amenity_id")
    private Amenity amenity;
}
