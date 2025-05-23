package com.wissen.hotel.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wissen.hotel.dtos.CreateHotelRequest;
import com.wissen.hotel.dtos.HotelResponse;
import com.wissen.hotel.dtos.UpdateHotelRequest;
import com.wissen.hotel.services.HotelService;
import com.wissen.hotel.services.ReviewService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HotelController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID hotelId;
    private HotelResponse mockHotel;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        mockHotel = HotelResponse.builder()
                .hotelId(hotelId)
                .name("Marriott")
                .description("Luxury hotel with premium amenities.")
                .address("123 Palm Street")
                .city("Bangalore")
                .state("Karnataka")
                .country("India")
                .latitude(BigDecimal.valueOf(12.9716))
                .longitude(BigDecimal.valueOf(77.5946))
                .isApproved(true)
                .createdAt(LocalDateTime.now())
                .ownerId(UUID.randomUUID())
                .build();
    }

    @Test
    void testGetHotelById() throws Exception {
        Mockito.when(hotelService.getHotelById(hotelId)).thenReturn(mockHotel);

        mockMvc.perform(get("/api/hotels/{id}",hotelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Marriott"))
                .andExpect(jsonPath("$.latitude").value(12.9716))
                .andExpect(jsonPath("$.longitude").value(77.5946))
                .andExpect(jsonPath("$.city").value("Bangalore"))
                .andExpect(jsonPath("$.approved").value(true));
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void testApproveHotelAsAdmin() throws Exception {
        Mockito.when(hotelService.approveHotel(hotelId)).thenReturn(mockHotel);

        mockMvc.perform(put("/api/hotels/{id}/approve", hotelId))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateHotel() throws Exception {
        CreateHotelRequest request = new CreateHotelRequest();
        request.setName("Marriott");
        request.setDescription("Luxury hotel");
        request.setAddress("123 Palm Street");
        request.setCity("Bangalore");

        Mockito.when(hotelService.createHotel(Mockito.any())).thenReturn(mockHotel);

        mockMvc.perform(post("/api/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Marriott"));
    }

    @Test
    void testUpdateHotel() throws Exception {
        UpdateHotelRequest request = new UpdateHotelRequest();
        request.setName("Updated Marriott");
        request.setDescription("Updated luxury hotel");

        mockHotel.setName("Updated Marriott");

        Mockito.when(hotelService.updateHotel(Mockito.eq(hotelId), Mockito.any())).thenReturn(mockHotel);

        mockMvc.perform(put("/api/hotels/{id}", hotelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Marriott"));
    }

    @Test
    void testGetAllHotels() throws Exception {
        Mockito.when(hotelService.getAllHotels(null, 0, 10)).thenReturn(List.of(mockHotel));

        mockMvc.perform(get("/api/hotels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Marriott"));
    }
}
