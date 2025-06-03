package com.wissen.hotel.controller;

import com.wissen.hotel.dto.request.CreateRoomRequest;
import com.wissen.hotel.dto.request.UpdateRoomRequest;
import com.wissen.hotel.dto.response.RoomResponse;
import com.wissen.hotel.service.RoomService;


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
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable("id") UUID id) {
        log.info("Fetching room by ID: {}", id);
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    // Add room to hotel
    @PostMapping("/hotel/{hotelId}")
    public ResponseEntity<RoomResponse> addRoomToHotel(
            @PathVariable("hotelId") UUID hotelId,
            @RequestBody CreateRoomRequest request
    ) {
        log.info("Adding room to hotel: {}", hotelId);
        return ResponseEntity.ok(roomService.createRoom(hotelId, request));
    }

    // Update room details
    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable("id") UUID id,
            @RequestBody UpdateRoomRequest request
    ) {
        log.info("Updating room: {}", id);
        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }

    // Delete room
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("id") UUID id) {
        log.info("Deleting room: {}", id);
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    // Check room availability
        @GetMapping("/{id}/availability")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable("id") UUID id,
            @RequestParam(name = "checkIn") LocalDate checkIn,
            @RequestParam(name = "checkOut") LocalDate checkOut) {
        log.info("Checking availability for room: {} from {} to {}", id, checkIn, checkOut);
        return ResponseEntity.ok(roomService.isRoomAvailable(id, checkIn, checkOut));
    }

    // Update amenities
    @PutMapping("/{id}/amenities")
    public ResponseEntity<RoomResponse> updateAmenities(
            @PathVariable("id") UUID id,
            @RequestBody List<String> amenities
    ) {
        log.info("Updating amenities for room: {}", id);
        return ResponseEntity.ok(roomService.updateRoomAmenities(id, amenities));
    }

    // Get all room types
    @GetMapping("/types")
    public ResponseEntity<List<String>> getRoomTypes() {
        log.info("Fetching all room types");
        return ResponseEntity.ok(roomService.getAllRoomTypes());
    }
}

