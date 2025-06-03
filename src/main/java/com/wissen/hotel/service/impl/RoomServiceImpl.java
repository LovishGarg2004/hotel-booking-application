package com.wissen.hotel.service.impl;

import com.wissen.hotel.dto.request.*;
import com.wissen.hotel.dto.response.*;
import com.wissen.hotel.service.UserService;
import com.wissen.hotel.enums.RoomType;
import com.wissen.hotel.exception.ResourceNotFoundException;
import com.wissen.hotel.model.*;
import com.wissen.hotel.repository.AmenityRepository;
import com.wissen.hotel.repository.HotelRepository;
import com.wissen.hotel.repository.RoomAmenityRepository;
import com.wissen.hotel.repository.RoomRepository;
import com.wissen.hotel.service.BookingService;
import com.wissen.hotel.service.RoomAmenityService;
import com.wissen.hotel.service.RoomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomAmenityRepository roomAmenityRepository;
    private final AmenityRepository amenityRepository;
    private final BookingService bookingService;
    private final RoomAmenityService roomAmenityService;

    private static final String ROOM_NOT_FOUND = "Room not found";

    @Override
    public RoomResponse createRoom(UUID hotelId, CreateRoomRequest request) {
        try {
            log.info("Creating room in hotel {}", hotelId);
            Hotel hotel = hotelRepository.findById(hotelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

            if (!hotel.isApproved()) {
                throw new IllegalStateException("Cannot create room in an unapproved hotel.");
            }

            Room room = Room.builder()
                    .hotel(hotel)
                    .roomType(request.getRoomType())
                    .capacity(request.getCapacity())
                    .basePrice(request.getBasePrice())
                    .totalRooms(request.getTotalRooms())
                    .build();

            Room savedRoom = roomRepository.save(room);
            if (savedRoom == null) {
                throw new IllegalStateException("Failed to save room. The repository returned null.");
            }

            return mapToResponse(savedRoom);
        } catch (ResourceNotFoundException | IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create room. Please try again later.", e);
        }
    }

    @Override
    public RoomResponse getRoomById(UUID roomId) {
        try {
            log.info("Fetching room details for roomId {}", roomId);
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND));
            return mapToResponse(room);
        } catch (ResourceNotFoundException | IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch room details. Please try again later.", e);
        }
    }

    @Override
    public RoomResponse updateRoom(UUID roomId, UpdateRoomRequest request) {
        try {
            log.info("Updating room {}", roomId);
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND));

            if (!room.getHotel().isApproved()) {
                throw new IllegalStateException("Cannot update room of an unapproved hotel.");
            }

            room.setRoomType(request.getRoomType());
            room.setCapacity(request.getCapacity());
            room.setBasePrice(request.getBasePrice());
            room.setTotalRooms(request.getTotalRooms());

            return mapToResponse(roomRepository.save(room));
        } catch (ResourceNotFoundException | IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update room. Please try again later.", e);
        }
    }

    @Override
    public void deleteRoom(UUID roomId) {
        try {
            log.info("Deleting room {}", roomId);
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND));

            if (!room.getHotel().isApproved()) {
                throw new IllegalStateException("Cannot delete room of an unapproved hotel.");
            }

            roomRepository.delete(room);
        } catch (ResourceNotFoundException | IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete room. Please try again later.", e);
        }
    }

    @Override
    public boolean isRoomAvailable(UUID roomId, LocalDate checkIn, LocalDate checkOut) {
        log.info("Checking availability for room {} from {} to {}", roomId, checkIn, checkOut);
        // Delegate to BookingServiceImpl's isRoomAvailable method
        // Assuming BookingServiceImpl is a Spring bean and injected here
        // Add: private final BookingService bookingService; at the top with other dependencies

        return bookingService.isRoomAvailable(roomId, checkIn, checkOut);
    }

    @Override
    public RoomResponse updateRoomAmenities(UUID roomId, List<String> amenities) {
        try {
            log.info("Updating room amenities for room {}", roomId);
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND));

            if (!room.getHotel().isApproved()) {
                throw new IllegalStateException("Cannot update amenities of a room in an unapproved hotel.");
            }

            room.getRoomAmenities().clear();

            List<RoomAmenity> updatedAmenities = amenities.stream()
                    .map(amenityName -> {
                        Amenity amenity = amenityRepository.findByName(amenityName);
                        if (amenity == null) {
                            throw new ResourceNotFoundException("Amenity not found: " + amenityName);
                        }
                        return RoomAmenity.builder()
                                .room(room)
                                .amenity(amenity)
                                .build();
                    })
                    .toList();

            room.getRoomAmenities().addAll(updatedAmenities);
            roomAmenityRepository.saveAll(updatedAmenities);

            Room savedRoom = roomRepository.save(room);
            if (savedRoom == null) {
                throw new IllegalStateException("Failed to save room. The repository returned null.");
            }

            return mapToResponse(savedRoom);
        } catch (ResourceNotFoundException | IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update room amenities. Please try again later.", e);
        }
    }

    @Override
    public List<String> getAllRoomTypes() {
        log.info("Fetching all room types");
        return Arrays.stream(RoomType.values())
                .map(Enum::name)
                .toList();
    }

    // ------------------ Helper ------------------
    private RoomResponse mapToResponse(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }

        List<AmenityResponse> amenities = roomAmenityService.getAmenitiesForRoom(room.getRoomId());

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
