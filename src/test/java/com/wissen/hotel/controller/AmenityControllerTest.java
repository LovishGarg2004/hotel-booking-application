package com.wissen.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wissen.hotel.dto.request.CreateOrUpdateAmenityRequest;
import com.wissen.hotel.dto.response.AmenityResponse;
import com.wissen.hotel.service.AmenityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AmenityController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
public class AmenityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AmenityService amenityService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID amenityId;
    private AmenityResponse amenityResponse;
    private CreateOrUpdateAmenityRequest amenityRequest;

    @BeforeEach
    void setUp() {
        amenityId = UUID.randomUUID();
        amenityResponse = AmenityResponse.builder()
                .amenityId(amenityId)
                .name("WiFi")
                .description("Free internet")
                .build();

        amenityRequest = CreateOrUpdateAmenityRequest.builder()
                .name("WiFi")
                .description("Free internet")
                .build();

    }

    @Test
    void testGetAllAmenities() throws Exception {
        List<AmenityResponse> amenities = List.of(amenityResponse);
        Mockito.when(amenityService.getAllAmenities()).thenReturn(amenities);

        mockMvc.perform(get("/api/amenities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amenityId").value(amenityId.toString()))
                .andExpect(jsonPath("$[0].name").value("WiFi"))
                .andExpect(jsonPath("$[0].description").value("Free internet"));
    }

    @Test
    void testGetAmenityById() throws Exception {
        Mockito.when(amenityService.getAmenityById(eq(amenityId))).thenReturn(amenityResponse);

        mockMvc.perform(get("/api/amenities/{id}", amenityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amenityId").value(amenityId.toString()))
                .andExpect(jsonPath("$.name").value("WiFi"))
                .andExpect(jsonPath("$.description").value("Free internet"));
    }

    @Test
    void testCreateAmenity() throws Exception {
        Mockito.when(amenityService.createAmenity(any(CreateOrUpdateAmenityRequest.class)))
                .thenReturn(amenityResponse);

        mockMvc.perform(post("/api/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amenityRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amenityId").value(amenityId.toString()))
                .andExpect(jsonPath("$.name").value("WiFi"))
                .andExpect(jsonPath("$.description").value("Free internet"));
    }

    @Test
    void testUpdateAmenity() throws Exception {
        Mockito.when(amenityService.updateAmenity(eq(amenityId), any(CreateOrUpdateAmenityRequest.class)))
                .thenReturn(amenityResponse);

        mockMvc.perform(put("/api/amenities/{id}", amenityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amenityRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amenityId").value(amenityId.toString()))
                .andExpect(jsonPath("$.name").value("WiFi"))
                .andExpect(jsonPath("$.description").value("Free internet"));
    }

    @Test
    void testDeleteAmenity() throws Exception {
        Mockito.doNothing().when(amenityService).deleteAmenity(eq(amenityId));

        mockMvc.perform(delete("/api/amenities/{id}", amenityId))
                .andExpect(status().isNoContent());
    }
}
