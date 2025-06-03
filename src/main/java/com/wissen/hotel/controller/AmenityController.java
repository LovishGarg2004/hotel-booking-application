package com.wissen.hotel.controller;

import com.wissen.hotel.dto.response.AmenityResponse;
import com.wissen.hotel.dto.request.CreateOrUpdateAmenityRequest;
import com.wissen.hotel.service.AmenityService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/amenities")
@RequiredArgsConstructor
@Tag(name = "Amenities Management", description = "Endpoints for managing hotel amenities")
public class AmenityController {

    private final AmenityService amenityService;

    @Operation(summary = "Get all amenities", description = "Public access")
    @GetMapping
    public ResponseEntity<List<AmenityResponse>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.getAllAmenities());
    }

    @Operation(summary = "Get amenity by ID", description = "Public access")
    @GetMapping("/{id}")
    public ResponseEntity<AmenityResponse> getAmenityById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(amenityService.getAmenityById(id));
    }

    @Operation(summary = "Create a new amenity", description = "Admin only")
    @PostMapping
    public ResponseEntity<AmenityResponse> createAmenity(@RequestBody CreateOrUpdateAmenityRequest request) {
        return ResponseEntity.ok(amenityService.createAmenity(request));
    }

    @Operation(summary = "Update an existing amenity", description = "Admin only")
    @PutMapping("/{id}")
    public ResponseEntity<AmenityResponse> updateAmenity(@PathVariable("id") UUID id, @RequestBody CreateOrUpdateAmenityRequest request) {
        return ResponseEntity.ok(amenityService.updateAmenity(id, request));
    }

    @Operation(summary = "Delete an amenity", description = "Admin only")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAmenity(@PathVariable("id") UUID id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }
}
