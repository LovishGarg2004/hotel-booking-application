package com.wissen.hotel.services;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.enums.RoomType;
import com.wissen.hotel.exceptions.ResourceNotFoundException;
import com.wissen.hotel.models.*;
import com.wissen.hotel.repositories.AmenityRepository;
import com.wissen.hotel.repositories.BookingRepository;
import com.wissen.hotel.repositories.HotelRepository;
import com.wissen.hotel.repositories.RoomAmenityRepository;
import com.wissen.hotel.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final RoomAmenityRepository roomAmenityRepository;
    private final AmenityRepository amenityRepository;

    @Override
    public RoomResponse createRoom(UUID hotelId, CreateRoomRequest request) {
        log.info("Creating room in hotel {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        Room room = Room.builder()
                .hotel(hotel)
                .roomType(request.getRoomType())
                .capacity(request.getCapacity())
                .basePrice(request.getBasePrice())
                .totalRooms(request.getTotalRooms())
                .build();

        Room savedRoom = roomRepository.save(room);
        return mapToResponse(savedRoom);
    }

    @Override
    public RoomResponse getRoomById(UUID roomId) {
        log.info("Fetching room details for roomId {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        return mapToResponse(room);
    }

    @Override
    public RoomResponse updateRoom(UUID roomId, UpdateRoomRequest request) {
        log.info("Updating room {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        room.setRoomType(request.getRoomType());
        room.setCapacity(request.getCapacity());
        room.setBasePrice(request.getBasePrice());
        room.setTotalRooms(request.getTotalRooms());

        return mapToResponse(roomRepository.save(room));
    }

    @Override
    public void deleteRoom(UUID roomId) {
        log.info("Deleting room {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        roomRepository.delete(room);
    }

    @Override
    public boolean isRoomAvailable(UUID roomId, LocalDate checkIn, LocalDate checkOut) {
        log.info("Checking availability for room {} from {} to {}", roomId, checkIn, checkOut);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        long bookedRooms = bookingRepository.findByRoom_RoomId(roomId).stream()
                .filter(b -> !(b.getCheckOut().isBefore(checkIn) || b.getCheckIn().isAfter(checkOut))) // Ensure 'b' is of type Booking
                .count();

        return bookedRooms < room.getTotalRooms();
    }

    @Override
    public RoomResponse updateRoomAmenities(UUID roomId, List<String> amenities) {
        log.info("Updating room amenities for room {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        room.getRoomAmenities().clear();
                List<RoomAmenity> updatedAmenities = amenities.stream()
                .map(amenityName -> {
                    Amenity amenity = amenityRepository.findByName(amenityName);
                    if (amenity == null) {
                        throw new ResourceNotFoundException("Amenity not found: " + amenityName);
                    }
                    return RoomAmenity.builder()
                            .room(room)
                            .amenity(amenity) // Associate with Amenity
                            .build();
                })
                .collect(Collectors.toList());

        room.getRoomAmenities().addAll(updatedAmenities);
        roomAmenityRepository.saveAll(updatedAmenities);

        return mapToResponse(roomRepository.save(room));
    }

        @Override
        public List<String> getAllRoomTypes() {
        log.info("Fetching all room types");
        return Arrays.stream(RoomType.values())
                        .map(Enum::name)
                        .collect(Collectors.toList());
        }


    // ------------------ Helper ------------------
    private RoomResponse mapToResponse(Room room) {
        List<String> amenities = room.getRoomAmenities() != null ?
                room.getRoomAmenities().stream().map(roomAmenity -> roomAmenity.getAmenity().getName()).collect(Collectors.toList()) :
                Collections.emptyList();

        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .hotelId(room.getHotel().getHotelId())
                .roomType(room.getRoomType())
                .capacity(room.getCapacity())
                .basePrice(room.getBasePrice())
                .totalRooms(room.getTotalRooms())
                .amenities(amenities)
                .build();
    }
}
