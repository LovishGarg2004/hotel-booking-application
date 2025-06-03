package com.wissen.hotel.controller;

import com.wissen.hotel.dto.request.CreateBookingRequest;
import com.wissen.hotel.dto.request.UpdateBookingRequest;
import com.wissen.hotel.dto.response.BookingResponse;
import com.wissen.hotel.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingControllerTest {

    private BookingService bookingService;
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        bookingService = mock(BookingService.class);
        bookingController = new BookingController(bookingService);
    }

    @Test
    void createBooking_shouldReturnCreatedWithResponse() {
        CreateBookingRequest request = new CreateBookingRequest();
        BookingResponse response = new BookingResponse();
        when(bookingService.createBooking(request)).thenReturn(response);

        ResponseEntity<BookingResponse> result = bookingController.createBooking(request);

        assertEquals(201, result.getStatusCode().value());
        assertSame(response, result.getBody());
        verify(bookingService, times(1)).createBooking(request);
    }

    @Test
    void getBooking_shouldReturnOkWithResponse() {
        UUID id = UUID.randomUUID();
        BookingResponse response = new BookingResponse();
        when(bookingService.getBookingById(id)).thenReturn(response);

        ResponseEntity<BookingResponse> result = bookingController.getBooking(id);

        assertEquals(200, result.getStatusCode().value());
        assertSame(response, result.getBody());
        verify(bookingService, times(1)).getBookingById(id);
    }

    @Test
    void updateBooking_shouldReturnOkWithResponse() {
        UUID id = UUID.randomUUID();
        UpdateBookingRequest request = new UpdateBookingRequest();
        BookingResponse response = new BookingResponse();
        when(bookingService.updateBooking(id, request)).thenReturn(response);

        ResponseEntity<BookingResponse> result = bookingController.updateBooking(id, request);

        assertEquals(200, result.getStatusCode().value());
        assertSame(response, result.getBody());
        verify(bookingService, times(1)).updateBooking(id, request);
    }

    @Test
    void approveBooking_shouldReturnOkWithResponse() {
        UUID id = UUID.randomUUID();
        BookingResponse response = new BookingResponse();
        when(bookingService.approveBooking(id)).thenReturn(response);

        ResponseEntity<BookingResponse> result = bookingController.approveBooking(id);

        assertEquals(200, result.getStatusCode().value());
        assertSame(response, result.getBody());
        verify(bookingService, times(1)).approveBooking(id);
    }

    @Test
    void cancelBooking_shouldReturnOkWithResponse() {
        UUID id = UUID.randomUUID();
        BookingResponse response = new BookingResponse();
        when(bookingService.cancelBooking(id)).thenReturn(response);

        ResponseEntity<BookingResponse> result = bookingController.cancelBooking(id);

        assertEquals(200, result.getStatusCode().value());
        assertSame(response, result.getBody());
        verify(bookingService, times(1)).cancelBooking(id);
    }

    @Test
    void getAllBookings_shouldReturnOkWithList() {
        List<BookingResponse> responses = List.of(new BookingResponse(), new BookingResponse());
        when(bookingService.getAllBookings(null)).thenReturn(responses);

        ResponseEntity<List<BookingResponse>> result = bookingController.getAllBookings(null);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(responses, result.getBody());
        verify(bookingService, times(1)).getAllBookings(null);
    }

    @Test
    void getAllBookings_withFilter_shouldReturnOkWithList() {
        String filter = "active";
        List<BookingResponse> responses = List.of(new BookingResponse());
        when(bookingService.getAllBookings(filter)).thenReturn(responses);

        ResponseEntity<List<BookingResponse>> result = bookingController.getAllBookings(filter);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(responses, result.getBody());
        verify(bookingService, times(1)).getAllBookings(filter);
    }

    @Test
    void getHotelBookings_shouldReturnOkWithList() {
        UUID hotelId = UUID.randomUUID();
        List<BookingResponse> responses = List.of(new BookingResponse());
        when(bookingService.getBookingsForHotel(hotelId)).thenReturn(responses);

        ResponseEntity<List<BookingResponse>> result = bookingController.getHotelBookings(hotelId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(responses, result.getBody());
        verify(bookingService, times(1)).getBookingsForHotel(hotelId);
    }

    @Test
    void generateInvoice_shouldReturnOkWithResponse() {
        UUID id = UUID.randomUUID();
        BookingResponse response = new BookingResponse();
        when(bookingService.generateInvoice(id)).thenReturn(response);

        ResponseEntity<BookingResponse> result = bookingController.generateInvoice(id);

        assertEquals(200, result.getStatusCode().value());
        assertSame(response, result.getBody());
        verify(bookingService, times(1)).generateInvoice(id);
    }
}