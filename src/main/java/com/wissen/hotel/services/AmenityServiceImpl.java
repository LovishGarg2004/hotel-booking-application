package com.wissen.hotel.services;

import com.wissen.hotel.dtos.AmenityResponse;
import com.wissen.hotel.dtos.CreateOrUpdateAmenityRequest;
import com.wissen.hotel.models.Amenity;
import com.wissen.hotel.repositories.AmenityRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AmenityServiceImpl implements AmenityService {

    private final AmenityRepository amenityRepository;

    private AmenityResponse mapToDto(Amenity amenity) {
        return AmenityResponse.builder()
                .amenityId(amenity.getAmenityId())
                .name(amenity.getName())
                .description(amenity.getDescription())
                .build();
    }

    @Override
    public List<AmenityResponse> getAllAmenities() {
        try {
            return amenityRepository.findAll().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch amenities. Please try again later.", e);
        }
    }

    @Override
    public AmenityResponse getAmenityById(UUID id) {
        try {
            Amenity amenity = amenityRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Amenity not found"));
            return mapToDto(amenity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch amenity. Please try again later.", e);
        }
    }

    @Override
    public AmenityResponse createAmenity(CreateOrUpdateAmenityRequest request) {
        try {
            Amenity amenity = Amenity.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();
            return mapToDto(amenityRepository.save(amenity));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create amenity. Please try again later.", e);
        }
    }

    @Override
    public AmenityResponse updateAmenity(UUID id, CreateOrUpdateAmenityRequest request) {
        try {
            Amenity amenity = amenityRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Amenity not found"));

            amenity.setName(request.getName());
            amenity.setDescription(request.getDescription());

            return mapToDto(amenityRepository.save(amenity));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update amenity. Please try again later.", e);
        }
    }

    @Override
    public void deleteAmenity(UUID id) {
        try {
            if (!amenityRepository.existsById(id)) {
                throw new RuntimeException("Amenity not found");
            }
            amenityRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete amenity. Please try again later.", e);
        }
    }
}
