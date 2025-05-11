package com.wissen.hotel.models;

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
public class RoomAvailability {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID availabilityId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private LocalDate date;
    private int availableRooms;
}
