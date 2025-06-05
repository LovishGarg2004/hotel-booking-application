package com.wissen.hotel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

import com.wissen.hotel.dto.request.*;
import com.wissen.hotel.dto.response.*;
import com.wissen.hotel.model.*;
import com.wissen.hotel.repository.*;
import com.wissen.hotel.service.impl.HotelServiceImpl;
import com.wissen.hotel.util.AuthUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomAvailabilityService roomAvailabilityService;

    @Mock
    private PricingEngineService pricingEngineService;

    @InjectMocks
    private HotelServiceImpl hotelService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private RoomAvailabilityRepository roomAvailabilityRepository;

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
                .isApproved(true)
                .owner(mockOwner) // Set mock owner
                .build();
        // Inject the mock RoomAvailabilityRepository into hotelService
        try {
            java.lang.reflect.Field field = hotelService.getClass().getDeclaredField("roomAvailabilityRepository");
            field.setAccessible(true);
            field.set(hotelService, roomAvailabilityRepository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    void testUpdateHotel_Success() {
        UpdateHotelRequest updateRequest = new UpdateHotelRequest();
        updateRequest.setName("Updated Hotel");
        updateRequest.setAddress("123 Updated St");
        updateRequest.setDescription("Updated description");
        updateRequest.setCity("Updated City");
        updateRequest.setState("Updated State");
        updateRequest.setCountry("Updated Country");
        updateRequest.setLatitude(new BigDecimal(11.11));
        updateRequest.setLongitude(new BigDecimal(22.22));

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(mockHotel));
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HotelResponse response = hotelService.updateHotel(hotelId, updateRequest);

        assertNotNull(response);
        assertEquals("Updated Hotel", response.getName());
        assertEquals("123 Updated St", response.getAddress());
        assertEquals("Updated description", response.getDescription());
        assertEquals("Updated City", response.getCity());
        assertEquals("Updated State", response.getState());
        assertEquals("Updated Country", response.getCountry());
        verify(hotelRepository, times(1)).findById(hotelId);
        verify(hotelRepository, times(1)).save(mockHotel);
    }

    @Test
    void testUpdateHotel_NotFound() {
        UpdateHotelRequest updateRequest = new UpdateHotelRequest();
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                hotelService.updateHotel(hotelId, updateRequest));
        assertEquals("Hotel not found", ex.getMessage());
        verify(hotelRepository, times(1)).findById(hotelId);
        verify(hotelRepository, never()).save(any());
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
                .totalRooms(5)
                .basePrice(new BigDecimal("100.00"))
                .build();

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(2);
        int numberOfGuests = 5;
        int neededRooms = (int) Math.ceil((double) numberOfGuests / mockRoom.getCapacity());

        when(hotelRepository.findAll()).thenReturn(hotels);
        when(roomRepository.findAllByHotel_HotelId(mockHotel.getHotelId())).thenReturn(List.of(mockRoom));
        // Simulate RoomAvailabilityRepository always returns enough rooms for all dates
        when(roomAvailabilityRepository.findByRoom_RoomIdAndDate(any(UUID.class), any(LocalDate.class)))
                .thenReturn(RoomAvailability.builder().availableRooms(5).build());
        // Simulate pricing engine
        PriceCalculationResponse priceResponse = new PriceCalculationResponse();
        priceResponse.setFinalPrice(new BigDecimal("100.00"));
        when(pricingEngineService.calculatePrice(mockRoom.getRoomId(), checkIn, checkOut)).thenReturn(priceResponse);

        List<HotelResponse> responses = hotelService.searchHotels("Test City", checkIn, checkOut, numberOfGuests, 0,
                10);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        HotelResponse resp = responses.get(0);
        assertEquals(mockHotel.getHotelId(), resp.getHotelId());
        assertEquals(neededRooms, resp.getRoomsRequired());
        assertEquals(new BigDecimal("100.00").multiply(BigDecimal.valueOf(neededRooms)), resp.getFinalPrice());
        verify(hotelRepository, times(1)).findAll();
        verify(roomRepository, times(1)).findAllByHotel_HotelId(mockHotel.getHotelId());
        verify(pricingEngineService, atLeastOnce()).calculatePrice(mockRoom.getRoomId(), checkIn, checkOut);
    }

    @Test
    void testGetAllHotels_Success() {
        // Mock rooms for the hotel
        Room mockRoom = Room.builder()
                .basePrice(new BigDecimal("100.00"))
                .hotel(mockHotel)
                .build();
        when(roomRepository.findAllByHotel_HotelId(mockHotel.getHotelId()))
                .thenReturn(List.of(mockRoom)); // <-- Return non-empty list

        // Mock hotel repository to return the approved hotel
        List<Hotel> hotels = List.of(mockHotel);
        when(hotelRepository.findAll()).thenReturn(hotels);

        // Test with matching city
        List<HotelResponse> responses = hotelService.getAllHotels("Test City", 0, 10);

        // Assertions
        assertNotNull(responses);
        assertEquals(1, responses.size()); // Now passes
        assertEquals("Test City", responses.get(0).getCity());
        assertEquals(mockHotel.getName(), responses.get(0).getName());
        verify(hotelRepository, times(1)).findAll();

        // Test with non-matching city
        List<HotelResponse> emptyResponses = hotelService.getAllHotels("Nonexistent City", 0, 10);
        assertTrue(emptyResponses.isEmpty());
    }

    @Test
    void testGetTopRatedHotels_Success() {
        Hotel hotel1 = Hotel.builder()
                .hotelId(UUID.randomUUID())
                .name("Hotel 1")
                .owner(mockOwner)
                .build();
        Hotel hotel2 = Hotel.builder()
                .hotelId(UUID.randomUUID())
                .name("Hotel 2")
                .owner(mockOwner)
                .build();
        Hotel hotel3 = Hotel.builder()
                .hotelId(UUID.randomUUID())
                .name("Hotel 3")
                .owner(mockOwner)
                .build();

        List<Hotel> hotels = Arrays.asList(hotel1, hotel2, hotel3);
        when(hotelRepository.findAll()).thenReturn(hotels);

        // Mock rooms - hotel3 has none
        Room room1 = Room.builder().basePrice(new BigDecimal("100.00")).hotel(hotel1).build();
        Room room2 = Room.builder().basePrice(new BigDecimal("80.00")).hotel(hotel2).build();
        when(roomRepository.findAllByHotel_HotelId(hotel1.getHotelId())).thenReturn(List.of(room1));
        when(roomRepository.findAllByHotel_HotelId(hotel2.getHotelId())).thenReturn(List.of(room2));
        when(roomRepository.findAllByHotel_HotelId(hotel3.getHotelId())).thenReturn(Collections.emptyList());

        // Mock reviews for getAverageRating
        when(reviewService.getReviewsByHotel(hotel1.getHotelId()))
                .thenReturn(List.of(
                        ReviewResponse.builder().rating(5).build(),
                        ReviewResponse.builder().rating(5).build()));
        when(reviewService.getReviewsByHotel(hotel2.getHotelId()))
                .thenReturn(List.of(
                        ReviewResponse.builder().rating(4).build(),
                        ReviewResponse.builder().rating(4).build()));

        List<HotelResponse> responses = hotelService.getTopRatedHotels();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(hotel1.getHotelId(), responses.get(0).getHotelId());
        assertEquals(hotel2.getHotelId(), responses.get(1).getHotelId());

        verify(hotelRepository).findAll();
        verify(roomRepository).findAllByHotel_HotelId(hotel1.getHotelId());
        verify(roomRepository).findAllByHotel_HotelId(hotel2.getHotelId());
        verify(roomRepository).findAllByHotel_HotelId(hotel3.getHotelId());
        verify(reviewService, atLeastOnce()).getReviewsByHotel(hotel1.getHotelId());
        verify(reviewService, atLeastOnce()).getReviewsByHotel(hotel2.getHotelId());
    }

    @Test
    void testFindNearbyHotels_Success() {
        mockHotel.setLatitude(BigDecimal.valueOf(12.9716));
        mockHotel.setLongitude(BigDecimal.valueOf(77.5946));
        List<Hotel> hotels = List.of(mockHotel);
        when(hotelRepository.findAll()).thenReturn(hotels);

        List<HotelResponse> responses = hotelService.findNearbyHotels(12.9716, 77.5946, 5);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void testGetHotelRooms_Success() {
        Room mockRoom = Room.builder()
                .roomId(UUID.randomUUID())
                .hotel(mockHotel)
                .capacity(2)
                .build();
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(mockHotel));
        when(roomRepository.findAllByHotel_HotelId(hotelId)).thenReturn(List.of(mockRoom));

        List<RoomResponse> responses = hotelService.getHotelRooms(hotelId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(mockRoom.getRoomId(), responses.get(0).getRoomId());
        verify(hotelRepository, times(1)).findById(hotelId);
        verify(roomRepository, times(1)).findAllByHotel_HotelId(hotelId);
    }

    @Test
    void testCheckAvailability_Success() {
        Room mockRoom = Room.builder()
                .roomId(UUID.randomUUID())
                .hotel(mockHotel)
                .capacity(2)
                .build();
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(mockHotel));
        when(roomRepository.findAllByHotel_HotelId(hotelId)).thenReturn(List.of(mockRoom));
        when(roomAvailabilityService.isRoomAvailableForRange(mockRoom.getRoomId(), LocalDate.now(),
                LocalDate.now().plusDays(1))).thenReturn(true);

        List<RoomResponse> responses = (List<RoomResponse>) hotelService.checkAvailability(hotelId,
                LocalDate.now().toString(), LocalDate.now().plusDays(1).toString());

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(hotelRepository, times(1)).findById(hotelId);
        verify(roomRepository, times(1)).findAllByHotel_HotelId(hotelId);
        verify(roomAvailabilityService, times(1)).isRoomAvailableForRange(mockRoom.getRoomId(), LocalDate.now(),
                LocalDate.now().plusDays(1));
    }

    @Test
    void testGetHotelsOwnedByCurrentUser_Success() {
        UUID currentUserId = mockOwner.getUserId();

        // Mock static method
        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getCurrentUser).thenReturn(mockOwner);

            when(hotelRepository.findAll()).thenReturn(List.of(mockHotel));

            List<HotelResponse> responses = hotelService.getHotelsOwnedByCurrentUser();

            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals(mockHotel.getOwner().getUserId(), currentUserId);
            verify(hotelRepository, times(1)).findAll();
        }
    }

    @Test
    void testGetAverageRating_Success() {
        ReviewResponse review1 = ReviewResponse.builder().rating(4).build();
        ReviewResponse review2 = ReviewResponse.builder().rating(5).build();
        when(reviewService.getReviewsByHotel(hotelId)).thenReturn(List.of(review1, review2));

        double averageRating = hotelService.getAverageRating(hotelId);

        assertEquals(4.5, averageRating);
        verify(reviewService, times(1)).getReviewsByHotel(hotelId);
    }
}
