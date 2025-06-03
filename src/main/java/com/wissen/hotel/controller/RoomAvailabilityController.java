package com.wissen.hotel.controller;

import com.wissen.hotel.service.RoomAvailabilityService;
import com.wissen.hotel.dto.request.UpdateInventoryRequest;
import com.wissen.hotel.dto.request.BlockRoomRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomAvailabilityController {

    private final RoomAvailabilityService roomAvailabilityService;

    @GetMapping("/{roomId}/availability/{date}")
    public ResponseEntity<Boolean> checkAvailabilityByDate(
            @PathVariable UUID roomId,
            @PathVariable LocalDate date) {
        
        return ResponseEntity.ok(roomAvailabilityService.isRoomAvailable(roomId, date));
    }

    @GetMapping("/{roomId}/availability/range")
    public ResponseEntity<Boolean> checkAvailabilityByDateRange(
            @PathVariable UUID roomId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(roomAvailabilityService.isRoomAvailableForRange(roomId, startDate, endDate));
    }

    @PutMapping("/{roomId}/inventory")
    public ResponseEntity<Void> updateRoomInventory(
            @PathVariable UUID roomId,
            @RequestBody UpdateInventoryRequest request) {
        roomAvailabilityService.updateInventory(roomId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomId}/block")
    public ResponseEntity<Void> blockRoomDates(
            @PathVariable UUID roomId,
            @RequestBody BlockRoomRequest request) {
        roomAvailabilityService.blockRoomDates(roomId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomId}/block")
    public ResponseEntity<Void> unblockRoomDates(
            @PathVariable UUID roomId,
            @RequestBody BlockRoomRequest request) {
        roomAvailabilityService.unblockRoomDates(roomId, request);
        return ResponseEntity.ok().build();
    }
}
