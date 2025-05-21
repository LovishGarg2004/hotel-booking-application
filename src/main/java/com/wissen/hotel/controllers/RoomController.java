package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.services.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final RoomService roomService;

    // Get room details
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable UUID id) {
        log.info("Fetching room by ID: {}", id);
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    // Add room to hotel
    @PostMapping("/hotel/{hotelId}")
    public ResponseEntity<RoomResponse> addRoomToHotel(
            @PathVariable UUID hotelId,
            @RequestBody CreateRoomRequest request
    ) {
        log.info("Adding room to hotel: {}", hotelId);
        // TODO: Authenticate hotel owner or admin
        return ResponseEntity.ok(roomService.createRoom(hotelId, request));
    }

    // Update room details
    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable UUID id,
            @RequestBody UpdateRoomRequest request
    ) {
        log.info("Updating room: {}", id);
        // TODO: Authenticate hotel owner or admin
        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }

    // Delete room
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID id) {
        log.info("Deleting room: {}", id);
        // TODO: Authenticate hotel owner or admin
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    // Check room availability
        @GetMapping("/{id}/availability")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable UUID id,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut) {
        log.info("Checking availability for room: {} from {} to {}", id, checkIn, checkOut);
        return ResponseEntity.ok(roomService.isRoomAvailable(id, checkIn, checkOut));
    }

    // Update amenities
    //TODO: After implementing the amenity service, this endpoint will be used to update the amenities of a room
    @PutMapping("/{id}/amenities")
    public ResponseEntity<RoomResponse> updateAmenities(
            @PathVariable UUID id,
            @RequestBody List<String> amenities
    ) {
        log.info("Updating amenities for room: {}", id);
        // TODO: Authenticate hotel owner or admin
        return ResponseEntity.ok(roomService.updateRoomAmenities(id, amenities));
    }

    // Get all room types
    @GetMapping("/types")
    public ResponseEntity<List<String>> getRoomTypes() {
        log.info("Fetching all room types");
        return ResponseEntity.ok(roomService.getAllRoomTypes());
    }
}

