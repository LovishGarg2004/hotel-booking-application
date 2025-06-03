package com.wissen.hotel.controller;

import org.springframework.web.bind.annotation.*;

import com.wissen.hotel.service.RoomAmenityService;
import com.wissen.hotel.dto.response.RoomAmenityResponse;
import com.wissen.hotel.dto.response.AmenityResponse;
import com.wissen.hotel.dto.request.AddAmenityToRoomRequest;

import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rooms/{roomId}/amenities")
@RequiredArgsConstructor
public class RoomAmenityController {

    private final RoomAmenityService roomAmenityService;

    @PostMapping
    public ResponseEntity<RoomAmenityResponse> addAmenityToRoom(
            @PathVariable UUID roomId,
            @RequestBody AddAmenityToRoomRequest request
    ) {
        return ResponseEntity.ok(roomAmenityService.addAmenityToRoom(roomId, request.getAmenityId()));
    }

    @DeleteMapping("/{amenityId}")
    public ResponseEntity<Void> removeAmenityFromRoom(
            @PathVariable UUID roomId,
            @PathVariable UUID amenityId
    ) {
        roomAmenityService.removeAmenityFromRoom(roomId, amenityId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<AmenityResponse>> getAmenitiesForRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok(roomAmenityService.getAmenitiesForRoom(roomId));
    }
}

