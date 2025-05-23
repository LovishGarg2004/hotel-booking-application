package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.ImageResponse;
import com.wissen.hotel.enums.ImageType;
import com.wissen.hotel.models.Image;
import com.wissen.hotel.services.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Image Management", description = "APIs for managing hotel and room images")
public class ImageController {
    private final CloudinaryService cloudinaryService;

    // General upload endpoint (kept for backward compatibility)
    @PostMapping(value = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload an image with reference ID and type")
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("referenceId") UUID referenceId,
            @RequestParam("type") ImageType type) throws IOException {
        Image image = cloudinaryService.uploadImage(referenceId, type, file);
        return ResponseEntity.ok(cloudinaryService.mapToImageResponse(image));
    }

    // Upload hotel image (for hotel owners/admins)
    @PostMapping(value = "/hotels/{hotelId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.isHotelOwner(#hotelId) or hasRole('ADMIN')")
    @Operation(summary = "Upload an image for a hotel")
    public ResponseEntity<ImageResponse> uploadHotelImage(
            @PathVariable UUID hotelId,
            @RequestParam("file") MultipartFile file) throws IOException {
        Image image = cloudinaryService.uploadImage(hotelId, ImageType.HOTEL, file);
        return ResponseEntity.ok(cloudinaryService.mapToImageResponse(image));
    }

    // Upload room image (for hotel owners/admins)
    @PostMapping(value = "/rooms/{roomId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.isRoomOwner(#roomId) or hasRole('ADMIN')")
    @Operation(summary = "Upload an image for a room")
    public ResponseEntity<ImageResponse> uploadRoomImage(
            @PathVariable UUID roomId,
            @RequestParam("file") MultipartFile file) throws IOException {
        Image image = cloudinaryService.uploadImage(roomId, ImageType.ROOM, file);
        return ResponseEntity.ok(cloudinaryService.mapToImageResponse(image));
    }

    // Get all images for a hotel (public)
    @GetMapping("/hotels/{hotelId}/images")
    @Operation(summary = "Get all images for a hotel")
    public ResponseEntity<List<ImageResponse>> getHotelImages(@PathVariable UUID hotelId) {
        List<Image> images = cloudinaryService.getImagesByReference(hotelId, ImageType.HOTEL);
        return ResponseEntity.ok(images.stream()
                .map(cloudinaryService::mapToImageResponse)
                .collect(Collectors.toList()));
    }

    // Get all images for a room (public)
    @GetMapping("/rooms/{roomId}/images")
    @Operation(summary = "Get all images for a room")
    public ResponseEntity<List<ImageResponse>> getRoomImages(@PathVariable UUID roomId) {
        List<Image> images = cloudinaryService.getImagesByReference(roomId, ImageType.ROOM);
        return ResponseEntity.ok(images.stream()
                .map(cloudinaryService::mapToImageResponse)
                .collect(Collectors.toList()));
    }

    // Get images by reference ID and type
    @GetMapping("/images/{referenceId}")
    @Operation(summary = "Get images by reference ID and optional type")
    public ResponseEntity<List<ImageResponse>> getImagesByReference(
            @PathVariable UUID referenceId,
            @RequestParam(required = false) ImageType type) {
        List<Image> images = cloudinaryService.getImagesByReference(referenceId, type);
        return ResponseEntity.ok(images.stream()
                .map(cloudinaryService::mapToImageResponse)
                .collect(Collectors.toList()));
    }

    // Get images by type
    @GetMapping("/images")
    @Operation(summary = "Get all images of a specific type")
    public ResponseEntity<List<ImageResponse>> getImagesByType(
            @RequestParam ImageType type) {
        List<Image> images = cloudinaryService.getImagesByType(type);
        return ResponseEntity.ok(images.stream()
                .map(cloudinaryService::mapToImageResponse)
                .collect(Collectors.toList()));
    }

    // Delete an image (owner or admin)
    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("@securityService.isImageOwner(#imageId) or hasRole('ADMIN')")
    @Operation(summary = "Delete an image by ID")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) throws IOException {
        cloudinaryService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }
}