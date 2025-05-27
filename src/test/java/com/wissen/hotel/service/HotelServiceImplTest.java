package com.wissen.hotel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.models.*;
import com.wissen.hotel.repositories.*;
import com.wissen.hotel.services.*;
import com.wissen.hotel.utils.AuthUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomAvailabilityService roomAvailabilityService;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private Hotel mockHotel;
    private UUID hotelId;
    private User mockOwner;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        mockOwner = User.builder()
                .userId(UUID.randomUUID())
                .name("Test Owner")
                .build();
        mockHotel = Hotel.builder()
                .hotelId(hotelId)
                .name("Test Hotel")
                .city("Test City")
                .isApproved(false)
                .owner(mockOwner) // Set mock owner
                .build();
    }

    @Test
    void testGetHotelById_Success() {
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(mockHotel));

        HotelResponse response = hotelService.getHotelById(hotelId);

        assertNotNull(response);
        assertEquals(mockHotel.getHotelId(), response.getHotelId());
        verify(hotelRepository, times(1)).findById(hotelId);
    }

    @Test
    void testGetHotelById_NotFound() {
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> hotelService.getHotelById(hotelId));

        assertEquals("Hotel not found", exception.getMessage());
        verify(hotelRepository, times(1)).findById(hotelId);
    }

    @Test
    void testCreateHotel_Success() {
        CreateHotelRequest request = new CreateHotelRequest();
        request.setName("New Hotel");
        request.setCity("New City");

        when(hotelRepository.save(any(Hotel.class))).thenReturn(mockHotel);

        HotelResponse response = hotelService.createHotel(request);

        assertNotNull(response);
        assertEquals(mockHotel.getName(), response.getName());
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void testDeleteHotel_Success() {
        doNothing().when(hotelRepository).deleteById(hotelId);

        hotelService.deleteHotel(hotelId);

        verify(hotelRepository, times(1)).deleteById(hotelId);
    }

    @Test
    void testApproveHotel_Success() {
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(mockHotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(mockHotel);

        HotelResponse response = hotelService.approveHotel(hotelId);

        assertNotNull(response);
        assertTrue(response.isApproved());
        verify(hotelRepository, times(1)).findById(hotelId);
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void testSearchHotels_Success() {
        List<Hotel> hotels = List.of(mockHotel);
        Room mockRoom = Room.builder()
                .roomId(UUID.randomUUID())
                .hotel(mockHotel)
                .capacity(3)
                .build();

        when(hotelRepository.findAll()).thenReturn(hotels);
        when(roomRepository.findAllByHotel_HotelId(mockHotel.getHotelId())).thenReturn(List.of(mockRoom));
        when(roomAvailabilityService.isRoomAvailableForRange(mockRoom.getRoomId(), LocalDate.now(), LocalDate.now().plusDays(1))).thenReturn(true);

        List<HotelResponse> responses = hotelService.searchHotels("Test City", LocalDate.now(), LocalDate.now().plusDays(1), 2, 0, 10);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(hotelRepository, times(1)).findAll();
        verify(roomRepository, times(1)).findAllByHotel_HotelId(mockHotel.getHotelId());
        verify(roomAvailabilityService, times(1)).isRoomAvailableForRange(mockRoom.getRoomId(), LocalDate.now(), LocalDate.now().plusDays(1));
    }
}
