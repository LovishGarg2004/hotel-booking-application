package com.wissen.hotel.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;

import com.wissen.hotel.repository.RoomAmenityRepository;
import com.wissen.hotel.repository.RoomRepository;
import com.wissen.hotel.service.RoomAmenityService;
import com.wissen.hotel.repository.AmenityRepository;
import com.wissen.hotel.model.RoomAmenity;
import com.wissen.hotel.model.Room;
import com.wissen.hotel.model.Amenity;
import com.wissen.hotel.dto.response.RoomAmenityResponse;
import com.wissen.hotel.dto.response.AmenityResponse;

@Service
@RequiredArgsConstructor
public class RoomAmenityServiceImpl implements RoomAmenityService {

    private final RoomAmenityRepository roomAmenityRepository;
    private final RoomRepository roomRepository;
    private final AmenityRepository amenityRepository;

    @Override
    public RoomAmenityResponse addAmenityToRoom(UUID roomId, UUID amenityId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new EntityNotFoundException("Amenity not found"));
        RoomAmenity roomAmenity = RoomAmenity.builder()
                .room(room)
                .amenity(amenity)
                .build();
        return mapToResponse(roomAmenityRepository.save(roomAmenity));
    }

    @Override
    public void removeAmenityFromRoom(UUID roomId, UUID amenityId) {
        RoomAmenity roomAmenity = roomAmenityRepository
                .findByRoom_RoomIdAndAmenity_AmenityId(roomId, amenityId)
                .orElseThrow(() -> new EntityNotFoundException("Amenity not assigned to room"));
        roomAmenityRepository.delete(roomAmenity);
    }

    @Override
    @Transactional
    public List<AmenityResponse> getAmenitiesForRoom(UUID roomId) {
        return roomAmenityRepository.findByRoom_RoomId(roomId).stream()
                .map(ra -> new AmenityResponse(
                        ra.getAmenity().getAmenityId(),
                        ra.getAmenity().getName(),
                        ra.getAmenity().getDescription()
                ))
                .distinct()
                .toList();
    }

    private RoomAmenityResponse mapToResponse(RoomAmenity roomAmenity) {
        return new RoomAmenityResponse(
                roomAmenity.getId(),
                roomAmenity.getRoom().getRoomId(),
                roomAmenity.getAmenity().getAmenityId(),
                roomAmenity.getAmenity().getName(),
                roomAmenity.getAmenity().getDescription()
        );
    }
}
