package com.wissen.hotel.service;

import com.wissen.hotel.dto.response.AmenityResponse;
import com.wissen.hotel.dto.request.CreateOrUpdateAmenityRequest;

import java.util.List;
import java.util.UUID;

public interface AmenityService {
    List<AmenityResponse> getAllAmenities();
    AmenityResponse getAmenityById(UUID id);
    AmenityResponse createAmenity(CreateOrUpdateAmenityRequest request);
    AmenityResponse updateAmenity(UUID id, CreateOrUpdateAmenityRequest request);
    void deleteAmenity(UUID id);
}
