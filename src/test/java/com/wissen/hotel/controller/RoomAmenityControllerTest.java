package com.wissen.hotel.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.wissen.hotel.controller.RoomAmenityController;
import com.wissen.hotel.service.RoomAmenityService;
import com.wissen.hotel.dto.request.AddAmenityToRoomRequest;
import com.wissen.hotel.dto.response.RoomAmenityResponse;
import com.wissen.hotel.dto.response.AmenityResponse;

class RoomAmenityControllerTest {

    @Mock
    private RoomAmenityService roomAmenityService;

    @InjectMocks
    private RoomAmenityController roomAmenityController;

    private UUID roomId;
    private UUID amenityId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roomId = UUID.randomUUID();
        amenityId = UUID.randomUUID();
    }

    @Test
    void testAddAmenityToRoom() {
        AddAmenityToRoomRequest request = mock(AddAmenityToRoomRequest.class);
        RoomAmenityResponse response = mock(RoomAmenityResponse.class);

        when(request.getAmenityId()).thenReturn(amenityId);
        when(roomAmenityService.addAmenityToRoom(roomId, amenityId)).thenReturn(response);

        ResponseEntity<RoomAmenityResponse> result = roomAmenityController.addAmenityToRoom(roomId, request);

        verify(roomAmenityService).addAmenityToRoom(roomId, amenityId);
        assertEquals(response, result.getBody());
    }

    @Test
    void testRemoveAmenityFromRoom() {
        ResponseEntity<Void> result = roomAmenityController.removeAmenityFromRoom(roomId, amenityId);

        verify(roomAmenityService).removeAmenityFromRoom(roomId, amenityId);
        assertEquals(204, result.getStatusCodeValue());
    }

    @Test
    void testGetAmenitiesForRoom() {
        List<AmenityResponse> amenities = Arrays.asList(mock(AmenityResponse.class), mock(AmenityResponse.class));

        when(roomAmenityService.getAmenitiesForRoom(roomId)).thenReturn(amenities);

        ResponseEntity<List<AmenityResponse>> result = roomAmenityController.getAmenitiesForRoom(roomId);

        verify(roomAmenityService).getAmenitiesForRoom(roomId);
        assertEquals(amenities, result.getBody());
    }
}
