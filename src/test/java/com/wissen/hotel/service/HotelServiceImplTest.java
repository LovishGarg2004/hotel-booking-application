package com.wissen.hotel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
                .isApproved(false)
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

        List<HotelResponse> responses = hotelService.searchHotels("Test City", checkIn, checkOut, numberOfGuests, 0, 10);

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
        List<Hotel> hotels = List.of(mockHotel);
        when(hotelRepository.findAll()).thenReturn(hotels);

        List<HotelResponse> responses = hotelService.getAllHotels("Test City", 0, 10);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(mockHotel.getName(), responses.get(0).getName());
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void testGetTopRatedHotels_Success() {
        List<Hotel> hotels = List.of(mockHotel);
        when(hotelRepository.findAll()).thenReturn(hotels);

        List<HotelResponse> responses = hotelService.getTopRatedHotels();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(hotelRepository, times(1)).findAll();
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
        when(roomAvailabilityService.isRoomAvailableForRange(mockRoom.getRoomId(), LocalDate.now(), LocalDate.now().plusDays(1))).thenReturn(true);

        List<RoomResponse> responses = (List<RoomResponse>) hotelService.checkAvailability(hotelId, LocalDate.now().toString(), LocalDate.now().plusDays(1).toString());

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(hotelRepository, times(1)).findById(hotelId);
        verify(roomRepository, times(1)).findAllByHotel_HotelId(hotelId);
        verify(roomAvailabilityService, times(1)).isRoomAvailableForRange(mockRoom.getRoomId(), LocalDate.now(), LocalDate.now().plusDays(1));
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
