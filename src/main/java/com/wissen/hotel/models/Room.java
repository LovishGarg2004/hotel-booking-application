package com.wissen.hotel.models;

import com.wissen.hotel.enums.RoomType;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID roomId;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    private int capacity;
    private BigDecimal basePrice;
    private int totalRooms;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<RoomAmenity> roomAmenities;
}
