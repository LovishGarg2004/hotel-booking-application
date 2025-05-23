package com.wissen.hotel.controllers;

import com.wissen.hotel.dtos.*;
import com.wissen.hotel.enums.RoomType;
import com.wissen.hotel.services.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    private UUID roomId;
    private UUID hotelId;
    private RoomResponse roomResponse;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        hotelId = UUID.randomUUID();
        roomResponse = RoomResponse.builder()
                .roomId(roomId)
                .hotelId(hotelId)
                .roomType(RoomType.SINGLE)
                .capacity(2)
                .basePrice(BigDecimal.valueOf(100.00))
                .totalRooms(10)
                .amenities(Arrays.asList("WiFi", "TV"))
                .build();
    }

    @Test
    void testGetRoomById() throws Exception {
        Mockito.when(roomService.getRoomById(roomId)).thenReturn(roomResponse);

        mockMvc.perform(get("/api/rooms/{id}", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId.toString()))
                .andExpect(jsonPath("$.hotelId").value(hotelId.toString()))
                .andExpect(jsonPath("$.roomType").value("SINGLE"))
                .andExpect(jsonPath("$.capacity").value(2))
                .andExpect(jsonPath("$.basePrice").value(100.00))
                .andExpect(jsonPath("$.totalRooms").value(10))
                .andExpect(jsonPath("$.amenities").isArray());
    }

    @Test
    void testAddRoomToHotel() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest(RoomType.SINGLE, 2, BigDecimal.valueOf(100.00), 10);
        Mockito.when(roomService.createRoom(eq(hotelId), any(CreateRoomRequest.class))).thenReturn(roomResponse);

        mockMvc.perform(post("/api/rooms/hotel/{hotelId}", hotelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "roomType": "SINGLE",
                                    "capacity": 2,
                                    "basePrice": 100.00,
                                    "totalRooms": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId.toString()));
    }

    @Test
    void testUpdateRoom() throws Exception {
        UpdateRoomRequest request = new UpdateRoomRequest(RoomType.DOUBLE, 3, BigDecimal.valueOf(150.00), 5);
        Mockito.when(roomService.updateRoom(eq(roomId), any(UpdateRoomRequest.class))).thenReturn(roomResponse);

        mockMvc.perform(put("/api/rooms/{id}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "roomType": "DOUBLE",
                                    "capacity": 3,
                                    "basePrice": 150.00,
                                    "totalRooms": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId.toString()));
    }

    @Test
    void testDeleteRoom() throws Exception {
        mockMvc.perform(delete("/api/rooms/{id}", roomId))
                .andExpect(status().isNoContent());

        Mockito.verify(roomService).deleteRoom(roomId);
    }

    @Test
    void testCheckAvailability() throws Exception {
        LocalDate checkIn = LocalDate.of(2025, 5, 20);
        LocalDate checkOut = LocalDate.of(2025, 5, 25);
        Mockito.when(roomService.isRoomAvailable(roomId, checkIn, checkOut)).thenReturn(true);

        mockMvc.perform(get("/api/rooms/{id}/availability", roomId)
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkOut.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testUpdateAmenities() throws Exception {
        List<String> amenities = Arrays.asList("WiFi", "TV");
        Mockito.when(roomService.updateRoomAmenities(eq(roomId), any(List.class))).thenReturn(roomResponse);

        mockMvc.perform(put("/api/rooms/{id}/amenities", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                ["WiFi", "TV"]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId.toString()));
    }

    @Test
    void testGetRoomTypes() throws Exception {
        List<String> roomTypes = Arrays.asList("SINGLE", "DOUBLE", "SUITE");
        Mockito.when(roomService.getAllRoomTypes()).thenReturn(roomTypes);

        mockMvc.perform(get("/api/rooms/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("SINGLE"))
                .andExpect(jsonPath("$[1]").value("DOUBLE"))
                .andExpect(jsonPath("$[2]").value("SUITE"));
    }
}