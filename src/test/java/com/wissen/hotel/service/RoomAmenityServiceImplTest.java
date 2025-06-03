package com.wissen.hotel.service;

import com.wissen.hotel.service.impl.RoomAmenityServiceImpl;
import com.wissen.hotel.dto.response.AmenityResponse;
import com.wissen.hotel.dto.response.RoomAmenityResponse;
import com.wissen.hotel.model.Amenity;
import com.wissen.hotel.model.Room;
import com.wissen.hotel.model.RoomAmenity;
import com.wissen.hotel.repository.AmenityRepository;
import com.wissen.hotel.repository.RoomAmenityRepository;
import com.wissen.hotel.repository.RoomRepository;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomAmenityServiceImplTest {

    @InjectMocks
    private RoomAmenityServiceImpl roomAmenityService;

    @Mock
    private RoomAmenityRepository roomAmenityRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private AmenityRepository amenityRepository;

    private UUID roomId;
    private UUID amenityId;
    private Room room;
    private Amenity amenity;
    private RoomAmenity roomAmenity;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        amenityId = UUID.randomUUID();
        room = Room.builder().roomId(roomId).build();
        amenity = Amenity.builder().amenityId(amenityId).name("WiFi").description("Wireless Internet").build();
        roomAmenity = RoomAmenity.builder().id(UUID.randomUUID()).room(room).amenity(amenity).build();
    }

    @Test
    void testAddAmenityToRoom_Success() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(amenity));
        when(roomAmenityRepository.save(any(RoomAmenity.class))).thenReturn(roomAmenity);

        RoomAmenityResponse response = roomAmenityService.addAmenityToRoom(roomId, amenityId);

        assertNotNull(response);
        assertEquals(roomId, response.getRoomId());
        assertEquals(amenityId, response.getAmenityId());
        assertEquals("WiFi", response.getAmenityName());
        verify(roomAmenityRepository).save(any(RoomAmenity.class));
    }

    @Test
    void testAddAmenityToRoom_RoomNotFound() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> roomAmenityService.addAmenityToRoom(roomId, amenityId));
        assertTrue(ex.getMessage().contains("Room not found"));
    }

    @Test
    void testAddAmenityToRoom_AmenityNotFound() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> roomAmenityService.addAmenityToRoom(roomId, amenityId));
        assertTrue(ex.getMessage().contains("Amenity not found"));
    }

    @Test
    void testRemoveAmenityFromRoom_Success() {
        when(roomAmenityRepository.findByRoom_RoomIdAndAmenity_AmenityId(roomId, amenityId))
                .thenReturn(Optional.of(roomAmenity));
        roomAmenityService.removeAmenityFromRoom(roomId, amenityId);
        verify(roomAmenityRepository).delete(roomAmenity);
    }

    @Test
    void testRemoveAmenityFromRoom_NotFound() {
        when(roomAmenityRepository.findByRoom_RoomIdAndAmenity_AmenityId(roomId, amenityId))
                .thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> roomAmenityService.removeAmenityFromRoom(roomId, amenityId));
        assertTrue(ex.getMessage().contains("Amenity not assigned to room"));
    }

    @Test
    void testGetAmenitiesForRoom() {
        when(roomAmenityRepository.findByRoom_RoomId(roomId)).thenReturn(List.of(roomAmenity));
        List<AmenityResponse> result = roomAmenityService.getAmenitiesForRoom(roomId);
        assertEquals(1, result.size());
        AmenityResponse ar = result.get(0);
        assertEquals(amenityId, ar.getAmenityId());
        assertEquals("WiFi", ar.getName());
        assertEquals("Wireless Internet", ar.getDescription());
    }
}
