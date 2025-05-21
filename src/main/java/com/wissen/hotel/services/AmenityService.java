package com.wissen.hotel.services;

import com.wissen.hotel.dtos.AmenityResponse;
import com.wissen.hotel.dtos.CreateOrUpdateAmenityRequest;

import java.util.List;
import java.util.UUID;

public interface AmenityService {
    List<AmenityResponse> getAllAmenities();
    AmenityResponse getAmenityById(UUID id);
    AmenityResponse createAmenity(CreateOrUpdateAmenityRequest request);
    AmenityResponse updateAmenity(UUID id, CreateOrUpdateAmenityRequest request);
    void deleteAmenity(UUID id);
}
