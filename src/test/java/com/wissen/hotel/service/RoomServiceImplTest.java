package com.wissen.hotel.service;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.enums.RoomType;
import com.wissen.hotel.exceptions.*;
import com.wissen.hotel.models.*;
import com.wissen.hotel.repositories.*;
import com.wissen.hotel.services.BookingService;
import com.wissen.hotel.services.RoomServiceImpl;
import com.wissen.hotel.services.RoomAmenityService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private RoomAmenityRepository roomAmenityRepository;
    @Mock
    private AmenityRepository amenityRepository;
    @Mock
    private BookingService bookingService;
    @Mock
    private RoomAmenityService roomAmenityService;

    @InjectMocks
    private RoomServiceImpl roomService;

    private UUID hotelId = UUID.randomUUID();
    private UUID roomId = UUID.randomUUID();
    private Hotel approvedHotel;
    private Room existingRoom;

    @BeforeEach
    void setUp() {
        approvedHotel = Hotel.builder()
                .hotelId(hotelId)
                .isApproved(true)
                .build();
        
        existingRoom = Room.builder()
                .roomId(roomId)
                .hotel(approvedHotel)
                .roomType(RoomType.SINGLE)
                .capacity(2)
                .basePrice(BigDecimal.valueOf(100.0))
                .totalRooms(5)
                .roomAmenities(new ArrayList<>()) 
                .build();
    }

    // Test Cases

    @Test
    void createRoom_ShouldSuccessfullyCreateRoom() {
        CreateRoomRequest request = new CreateRoomRequest(
            RoomType.SINGLE, 
            2, 
            BigDecimal.valueOf(100.0), // Convert double to BigDecimal
            5
        );
        
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(approvedHotel));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomResponse response = roomService.createRoom(hotelId, request);

        assertNotNull(response);
        assertEquals(RoomType.SINGLE, response.getRoomType()); // Updated expectation
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void createRoom_ShouldThrowWhenHotelNotApproved() {
        Hotel unapprovedHotel = Hotel.builder().isApproved(false).build();
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(unapprovedHotel));

        assertThrows(IllegalStateException.class, 
            () -> roomService.createRoom(hotelId, new CreateRoomRequest()));
    }

    @Test
    void getRoomById_ShouldReturnRoom() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
        
        RoomResponse response = roomService.getRoomById(roomId);
        
        assertEquals(roomId, response.getRoomId());
        assertEquals(approvedHotel.getHotelId(), response.getHotelId());
    }

    @Test
    void updateRoom_ShouldUpdateFieldsSuccessfully() {
        UpdateRoomRequest request = new UpdateRoomRequest(
                RoomType.SUITE, 3, BigDecimal.valueOf(300.0), 10
        );
        
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(existingRoom);

        RoomResponse response = roomService.updateRoom(roomId, request);
        
        assertEquals(RoomType.SUITE, response.getRoomType());
        assertEquals(BigDecimal.valueOf(300.0), response.getBasePrice()); // Updated to use BigDecimal
    }

    @Test
    void deleteRoom_ShouldThrowWhenHotelNotApproved() {
        Hotel unapprovedHotel = Hotel.builder().isApproved(false).build();
        existingRoom.setHotel(unapprovedHotel);
        
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));

        assertThrows(IllegalStateException.class, 
            () -> roomService.deleteRoom(roomId));
    }

    @Test
    void isRoomAvailable_ShouldDelegateToBookingService() {
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(2);
        
        when(bookingService.isRoomAvailable(roomId, checkIn, checkOut)).thenReturn(true);
        
        assertTrue(roomService.isRoomAvailable(roomId, checkIn, checkOut));
    }

    @Test
    void updateRoomAmenities_ShouldUpdateAmenitiesSuccessfully() {
        List<String> amenities = List.of("WiFi", "TV");

        Amenity wifi = Amenity.builder().name("WiFi").build();
        Amenity tv = Amenity.builder().name("TV").build();

        existingRoom.setRoomAmenities(new ArrayList<>()); // reset just in case

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
        when(amenityRepository.findByName("WiFi")).thenReturn(wifi);
        when(amenityRepository.findByName("TV")).thenReturn(tv);
        when(roomAmenityRepository.saveAll(anyList())).thenReturn(Collections.emptyList()); // Mock saveAll to avoid null pointer
        when(roomRepository.save(any(Room.class))).thenReturn(existingRoom); // Mock roomRepository.save

        List<AmenityResponse> amenityResponses = List.of(
            new AmenityResponse(UUID.randomUUID(), "WiFi", "Wireless Internet"),
            new AmenityResponse(UUID.randomUUID(), "TV", "Television")
        );
        when(roomAmenityService.getAmenitiesForRoom(roomId)).thenReturn(amenityResponses);

        RoomResponse response = roomService.updateRoomAmenities(roomId, amenities);

        assertNotNull(response);
        assertEquals(2, response.getAmenities().size());
        assertTrue(response.getAmenities().stream().anyMatch(a -> a.getName().equals("WiFi")));
        assertTrue(response.getAmenities().stream().anyMatch(a -> a.getName().equals("TV")));
        verify(roomAmenityRepository).saveAll(anyList());
    }

    @Test
    void updateRoomAmenities_ShouldThrowWhenAmenityNotFound() {
        existingRoom.setRoomAmenities(new ArrayList<>());

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
        when(amenityRepository.findByName("Pool")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
            () -> roomService.updateRoomAmenities(roomId, List.of("Pool")));
    }



    @Test
    void getAllRoomTypes_ShouldReturnAllEnums() {
        List<String> types = roomService.getAllRoomTypes();
        
        assertEquals(RoomType.values().length, types.size());
        assertTrue(types.contains("SINGLE")); // Updated to match actual enum values
        assertTrue(types.contains("SUITE"));
    }

    // Edge Cases and Additional Coverage
    
    @Test
    void createRoom_ShouldThrowWhenHotelNotFound() {
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class,
            () -> roomService.createRoom(hotelId, new CreateRoomRequest()));
    }

    @Test
    void updateRoom_ShouldThrowWhenRoomNotFound() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class,
            () -> roomService.updateRoom(roomId, new UpdateRoomRequest()));
    }

    @Test
    void mapToResponse_ShouldHandleNullAmenities() {
        existingRoom.setRoomAmenities(null);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
        
        RoomResponse response = roomService.getRoomById(roomId);
        assertTrue(response.getAmenities().isEmpty());
    }
}
