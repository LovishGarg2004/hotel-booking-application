package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.BlockRoomRequest;
import com.wissen.hotel.dtos.UpdateInventoryRequest;
import com.wissen.hotel.services.RoomAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomAvailabilityControllerTest {

    private RoomAvailabilityService roomAvailabilityService;
    private RoomAvailabilityController controller;

    @BeforeEach
    void setUp() {
        roomAvailabilityService = mock(RoomAvailabilityService.class);
        controller = new RoomAvailabilityController(roomAvailabilityService);
    }

    @Test
    void checkAvailabilityByDate_shouldReturnAvailability() {
        UUID roomId = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        when(roomAvailabilityService.isRoomAvailable(roomId, date)).thenReturn(true);

        ResponseEntity<Boolean> result = controller.checkAvailabilityByDate(roomId, date);

        assertEquals(200, result.getStatusCodeValue());
        assertTrue(result.getBody());
        verify(roomAvailabilityService, times(1)).isRoomAvailable(roomId, date);
    }

    @Test
    void checkAvailabilityByDateRange_shouldReturnAvailability() {
        UUID roomId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        when(roomAvailabilityService.isRoomAvailableForRange(roomId, startDate, endDate)).thenReturn(false);

        ResponseEntity<Boolean> result = controller.checkAvailabilityByDateRange(roomId, startDate, endDate);

        assertEquals(200, result.getStatusCodeValue());
        assertFalse(result.getBody());
        verify(roomAvailabilityService, times(1)).isRoomAvailableForRange(roomId, startDate, endDate);
    }

    @Test
    void updateRoomInventory_shouldReturnOk() {
        UUID roomId = UUID.randomUUID();
        UpdateInventoryRequest request = new UpdateInventoryRequest();

        ResponseEntity<Void> result = controller.updateRoomInventory(roomId, request);

        assertEquals(200, result.getStatusCodeValue());
        verify(roomAvailabilityService, times(1)).updateInventory(roomId, request);
    }

    @Test
    void blockRoomDates_shouldReturnOk() {
        UUID roomId = UUID.randomUUID();
        BlockRoomRequest request = new BlockRoomRequest();

        ResponseEntity<Void> result = controller.blockRoomDates(roomId, request);

        assertEquals(200, result.getStatusCodeValue());
        verify(roomAvailabilityService, times(1)).blockRoomDates(roomId, request);
    }

    @Test
    void unblockRoomDates_shouldReturnOk() {
        UUID roomId = UUID.randomUUID();
        BlockRoomRequest request = new BlockRoomRequest();

        ResponseEntity<Void> result = controller.unblockRoomDates(roomId, request);

        assertEquals(200, result.getStatusCodeValue());
        verify(roomAvailabilityService, times(1)).unblockRoomDates(roomId, request);
    }
}