package com.wissen.hotel.service;

import com.wissen.hotel.dto.request.BlockRoomRequest;
import com.wissen.hotel.dto.request.UpdateInventoryRequest;
import com.wissen.hotel.exception.ResourceNotFoundException;
import com.wissen.hotel.model.Room;
import com.wissen.hotel.model.RoomAvailability;
import com.wissen.hotel.repository.RoomAvailabilityRepository;
import com.wissen.hotel.repository.RoomRepository;
import com.wissen.hotel.service.impl.RoomAvailabilityServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomAvailabilityServiceImplTest {

    @Mock
    private RoomAvailabilityRepository availabilityRepository;
    
    @Mock
    private RoomRepository roomRepository;
    
    @InjectMocks
    private RoomAvailabilityServiceImpl service;
    
    private UUID roomId;
    private UUID hotelId;
    private Room room;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        hotelId = UUID.randomUUID();
        today = LocalDate.now();
        room = new Room();
        room.setRoomId(roomId);
        room.setTotalRooms(10);
    }

    @Test
    void isRoomAvailable_ShouldReturnTrueWhenNoAvailabilityRecord() {
        when(availabilityRepository.findByRoom_RoomIdAndDate(roomId, today))
            .thenReturn(null);
        
        assertTrue(service.isRoomAvailable(roomId, today));
    }

    @Test
    void isRoomAvailable_ShouldReturnTrueWhenRoomsAvailable() {
        RoomAvailability availability = new RoomAvailability();
        availability.setAvailableRooms(5);
        when(availabilityRepository.findByRoom_RoomIdAndDate(roomId, today))
            .thenReturn(availability);
        
        assertTrue(service.isRoomAvailable(roomId, today));
    }

    @Test
    void isRoomAvailable_ShouldReturnFalseWhenNoRoomsAvailable() {
        RoomAvailability availability = new RoomAvailability();
        availability.setAvailableRooms(0);
        when(availabilityRepository.findByRoom_RoomIdAndDate(roomId, today))
            .thenReturn(availability);
        
        assertFalse(service.isRoomAvailable(roomId, today));
    }

    @Test
    void isRoomAvailableForRange_ShouldReturnTrueForFullAvailability() {
        LocalDate endDate = today.plusDays(3);
        when(availabilityRepository.findByRoom_RoomIdAndDate(any(), any()))
            .thenReturn(null);
        
        assertTrue(service.isRoomAvailableForRange(roomId, today, endDate));
    }

    @Test
    void isRoomAvailableForRange_ShouldReturnFalseForPartialAvailability() {
        LocalDate endDate = today.plusDays(3);
        when(availabilityRepository.findByRoom_RoomIdAndDate(eq(roomId), any(LocalDate.class)))
            .thenAnswer(invocation -> {
                LocalDate date = invocation.getArgument(1);
                if (date.equals(today.plusDays(1))) {
                    RoomAvailability unavailable = new RoomAvailability();
                    unavailable.setAvailableRooms(0);
                    return unavailable;
                }
                return null;
            });
        
        assertFalse(service.isRoomAvailableForRange(roomId, today, endDate));
    }

    @Test
    void updateInventory_ShouldCreateNewAvailabilityRecord() {
        UpdateInventoryRequest request = new UpdateInventoryRequest(today, 3);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(availabilityRepository.findByRoom_RoomIdAndDate(roomId, today))
            .thenReturn(null);
        
        service.updateInventory(roomId, request);
        
        verify(availabilityRepository).save(argThat(a -> 
            a.getAvailableRooms() == 7 && 
            a.getDate().equals(today)
        ));
    }

    @Test
    void updateInventory_ShouldUpdateExistingAvailabilityRecord() {
        UpdateInventoryRequest request = new UpdateInventoryRequest(today, 2);
        RoomAvailability existing = new RoomAvailability();
        existing.setAvailableRooms(5);
        
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(availabilityRepository.findByRoom_RoomIdAndDate(roomId, today))
            .thenReturn(existing);
        
        service.updateInventory(roomId, request);
        
        assertEquals(3, existing.getAvailableRooms());
        verify(availabilityRepository).save(existing);
    }

    @Test
    void updateInventory_ShouldThrowWhenExceedingTotalRooms() {
        UpdateInventoryRequest request = new UpdateInventoryRequest(today, 15);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        
        assertThrows(IllegalArgumentException.class, 
            () -> service.updateInventory(roomId, request));
    }

    @Test
    void blockRoomDates_ShouldCreateNewBlockedRecords() {
        BlockRoomRequest request = new BlockRoomRequest(today, today.plusDays(2));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        
        service.blockRoomDates(roomId, request);
        
        verify(availabilityRepository, times(3))
            .save(argThat(a -> a.getAvailableRooms() == 0));
    }

    @Test
    void unblockRoomDates_ShouldRestoreAvailability() {
        BlockRoomRequest request = new BlockRoomRequest(today, today.plusDays(1));
        RoomAvailability blocked = new RoomAvailability();
        blocked.setAvailableRooms(0);
        
        when(availabilityRepository.findByRoom_RoomIdAndDate(roomId, today))
            .thenReturn(blocked);
        
        service.unblockRoomDates(roomId, request);
        
        assertEquals(1, blocked.getAvailableRooms());
        verify(availabilityRepository).save(blocked);
    }

    @Test
    void getHotelAvailabilityRatio_ShouldCalculateCorrectRatio() {
        List<Room> rooms = List.of(
            createRoomWithTotal(5),
            createRoomWithTotal(5)
        );
        LocalDate checkIn = today;
        LocalDate checkOut = today.plusDays(2);
        
        when(roomRepository.findAllByHotel_HotelId(hotelId)).thenReturn(rooms);
        when(availabilityRepository.findByRoom_RoomIdAndDate(any(), any()))
            .thenReturn(null);
        
        double ratio = service.getHotelAvailabilityRatio(hotelId, checkIn, checkOut);
        
        assertEquals(1.0, ratio);
    }

    @Test
    void getHotelAvailabilityRatio_ShouldHandlePartialAvailability() {
        List<Room> rooms = List.of(createRoomWithTotal(5));
        LocalDate checkIn = today;
        LocalDate checkOut = today.plusDays(1);
        
        RoomAvailability availability = new RoomAvailability();
        availability.setAvailableRooms(3);
        
        when(roomRepository.findAllByHotel_HotelId(hotelId)).thenReturn(rooms);
        when(availabilityRepository.findByRoom_RoomIdAndDate(any(), any()))
            .thenReturn(availability);
        
        double ratio = service.getHotelAvailabilityRatio(hotelId, checkIn, checkOut);
        assertEquals(0.6, ratio, 0.01);
    }

    private Room createRoomWithTotal(int totalRooms) {
        Room r = new Room();
        r.setRoomId(UUID.randomUUID());
        r.setTotalRooms(totalRooms);
        return r;
    }

    @Test
    void updateInventory_ShouldThrowWhenRoomNotFound() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());
        UpdateInventoryRequest request = new UpdateInventoryRequest(today, 2);
        
        assertThrows(ResourceNotFoundException.class,
            () -> service.updateInventory(roomId, request));
    }

    @Test
    void getHotelAvailabilityRatio_ShouldReturnZeroForNoRooms() {
        when(roomRepository.findAllByHotel_HotelId(hotelId)).thenReturn(Collections.emptyList());
        double ratio = service.getHotelAvailabilityRatio(hotelId, today, today.plusDays(1));
        assertEquals(0.0, ratio);
    }
}
