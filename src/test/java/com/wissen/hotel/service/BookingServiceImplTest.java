package com.wissen.hotel.service;

import com.wissen.hotel.dtos.BookingResponse;
import com.wissen.hotel.dtos.CreateBookingRequest;
import com.wissen.hotel.dtos.UpdateBookingRequest;
import com.wissen.hotel.enums.BookingStatus;
import com.wissen.hotel.exceptions.BadRequestException;
import com.wissen.hotel.exceptions.ResourceNotFoundException;
import com.wissen.hotel.models.Booking;
import com.wissen.hotel.models.Room;
import com.wissen.hotel.models.User;
import com.wissen.hotel.repositories.BookingRepository;
import com.wissen.hotel.repositories.RoomRepository;
import com.wissen.hotel.services.BookingServiceImpl;
import com.wissen.hotel.services.RoomAvailabilityService;
import com.wissen.hotel.utils.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomAvailabilityService roomAvailabilityService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Room mockRoom;
    private User mockUser;
    private Booking mockBooking;

    @BeforeEach
    void setUp() {
        UUID roomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();

        mockRoom = new Room();
        mockRoom.setRoomId(roomId);
        mockRoom.setBasePrice(BigDecimal.valueOf(100));

        mockUser = new User();
        mockUser.setUserId(userId);

        mockBooking = Booking.builder()
                .bookingId(bookingId)
                .room(mockRoom)
                .user(mockUser)
                .checkIn(LocalDate.now().plusDays(1))
                .checkOut(LocalDate.now().plusDays(3))
                .guests(2)
                .roomsBooked(1)
                .status(BookingStatus.PENDING)
                .finalPrice(BigDecimal.valueOf(200))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateBooking_Success() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getCurrentUser).thenReturn(mockUser);
    
            CreateBookingRequest request = new CreateBookingRequest();
            request.setRoomId(mockRoom.getRoomId());
            LocalDate today = LocalDate.now();
            request.setCheckIn(today.plusDays(2));
            request.setCheckOut(today.plusDays(4));
            request.setGuests(2);
            request.setRoomsBooked(1);
    
            when(roomRepository.findById(mockRoom.getRoomId())).thenReturn(Optional.of(mockRoom));
            when(roomAvailabilityService.isRoomAvailableForRange(any(), any(), any())).thenReturn(true);
    
            // Capture saved booking and set ID
            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
                Booking b = invocation.getArgument(0);
                b.setBookingId(mockBooking.getBookingId());
                return b;
            });
    
            // Stub with explicit ID
            when(bookingRepository.findById(mockBooking.getBookingId()))
                .thenReturn(Optional.of(mockBooking));
    
            BookingResponse response = bookingService.createBooking(request);
    
            assertNotNull(response);
            assertEquals(mockBooking.getBookingId(), response.getBookingId());
            verify(bookingRepository).findById(mockBooking.getBookingId());
        }
    }

        @Test
    void testCreateBooking_RoomNotFound() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setRoomId(UUID.randomUUID());
    
        when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.empty());
    
        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(request));
    }

        @Test
    void testCreateBooking_RoomNotAvailable() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setRoomId(mockRoom.getRoomId());
        request.setCheckIn(LocalDate.now().plusDays(1));
        request.setCheckOut(LocalDate.now().plusDays(3));
    
        when(roomRepository.findById(mockRoom.getRoomId())).thenReturn(Optional.of(mockRoom));
        when(roomAvailabilityService.isRoomAvailableForRange(any(), any(), any())).thenReturn(false);
    
        assertThrows(BadRequestException.class, () -> bookingService.createBooking(request));
    }
    
        @Test
    void testGetBookingById_Success() {
        when(bookingRepository.findById(mockBooking.getBookingId())).thenReturn(Optional.of(mockBooking));
    
        BookingResponse response = bookingService.getBookingById(mockBooking.getBookingId());
    
        assertNotNull(response);
        assertEquals(mockBooking.getBookingId(), response.getBookingId());
    }

        @Test
    void testGetBookingById_NotFound() {
        when(bookingRepository.findById(mockBooking.getBookingId())).thenReturn(Optional.empty());
    
        assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingById(mockBooking.getBookingId()));
    }

        @Test
    void testUpdateBooking_Success() {
        UpdateBookingRequest request = new UpdateBookingRequest();
        request.setCheckIn(LocalDate.now().plusDays(2));
        request.setCheckOut(LocalDate.now().plusDays(4));
        request.setGuests(3);
        request.setRoomsBooked(2);
    
        when(bookingRepository.findById(mockBooking.getBookingId())).thenReturn(Optional.of(mockBooking));
        when(roomAvailabilityService.isRoomAvailableForRange(any(), any(), any())).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);
    
        BookingResponse response = bookingService.updateBooking(mockBooking.getBookingId(), request);
    
        assertNotNull(response);
        assertEquals(mockBooking.getBookingId(), response.getBookingId());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

        @Test
    void testUpdateBooking_NotFound() {
        UpdateBookingRequest request = new UpdateBookingRequest();
        when(bookingRepository.findById(mockBooking.getBookingId())).thenReturn(Optional.empty());
    
        assertThrows(ResourceNotFoundException.class, () -> bookingService.updateBooking(mockBooking.getBookingId(), request));
    }


    @Test
    void testCancelBooking_Success() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getCurrentUser).thenReturn(mockUser);

            when(bookingRepository.findById(mockBooking.getBookingId())).thenReturn(Optional.of(mockBooking));

            BookingResponse response = bookingService.cancelBooking(mockBooking.getBookingId());

            assertNotNull(response);
            assertEquals(BookingStatus.CANCELLED, response.getStatus());
            verify(bookingRepository, times(1)).save(any(Booking.class));
        }
    }

        @Test
    void testCancelBooking_NotFound() {
        when(bookingRepository.findById(mockBooking.getBookingId())).thenReturn(Optional.empty());
    
        assertThrows(ResourceNotFoundException.class, () -> bookingService.cancelBooking(mockBooking.getBookingId()));
    }

    @Test
    void testApproveBooking_InvalidStatus() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getCurrentUser).thenReturn(mockUser);

            mockBooking.setStatus(BookingStatus.CONFIRMED); // already confirmed

            when(bookingRepository.findById(mockBooking.getBookingId())).thenReturn(Optional.of(mockBooking));

            assertThrows(IllegalStateException.class,
                    () -> bookingService.approveBooking(mockBooking.getBookingId()));
        }
    }

        @Test
    void testApproveBooking_Success() {
        mockBooking.setStatus(BookingStatus.PENDING);
    
        when(bookingRepository.findById(mockBooking.getBookingId())).thenReturn(Optional.of(mockBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);
    
        BookingResponse response = bookingService.approveBooking(mockBooking.getBookingId());
    
        assertNotNull(response);
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void testGetAllBookings() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getCurrentUser).thenReturn(mockUser);

            when(bookingRepository.findAll()).thenReturn(List.of(mockBooking));

            List<BookingResponse> responses = bookingService.getAllBookings(null);

            assertNotNull(responses);
            assertEquals(1, responses.size());
            verify(bookingRepository, times(1)).findAll();
        }
    }

        @Test
    void testGetBookingsForHotel_Success() {
        UUID hotelId = UUID.randomUUID();
        when(bookingRepository.findByRoom_Hotel_HotelId(hotelId)).thenReturn(List.of(mockBooking));
    
        List<BookingResponse> responses = bookingService.getBookingsForHotel(hotelId);
    
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(bookingRepository, times(1)).findByRoom_Hotel_HotelId(hotelId);
    }

        @Test
    void testGenerateInvoice_Success() {
        when(bookingRepository.findById(mockBooking.getBookingId())).thenReturn(Optional.of(mockBooking));
    
        BookingResponse response = bookingService.generateInvoice(mockBooking.getBookingId());
    
        assertNotNull(response);
        assertEquals(mockBooking.getBookingId(), response.getBookingId());
    }

        @Test
    void testGenerateInvoice_NotFound() {
        when(bookingRepository.findById(mockBooking.getBookingId())).thenReturn(Optional.empty());
    
        assertThrows(ResourceNotFoundException.class, () -> bookingService.generateInvoice(mockBooking.getBookingId()));
    }

        @Test
    void testIsRoomAvailable_Success() {
        when(bookingRepository.findByRoom_RoomId(mockRoom.getRoomId())).thenReturn(Collections.emptyList());
        when(roomAvailabilityService.isRoomAvailableForRange(any(), any(), any())).thenReturn(true);
    
        boolean isAvailable = bookingService.isRoomAvailable(mockRoom.getRoomId(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
    
        assertTrue(isAvailable);
    }
        @Test
    void testIsRoomAvailable_NotAvailable() {
        when(bookingRepository.findByRoom_RoomId(mockRoom.getRoomId())).thenReturn(List.of(mockBooking));
    
        boolean isAvailable = bookingService.isRoomAvailable(mockRoom.getRoomId(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
    
        assertFalse(isAvailable);
    }

}
