package com.wissen.hotel.service;

import com.wissen.hotel.dto.response.AmenityResponse;
import com.wissen.hotel.dto.request.CreateOrUpdateAmenityRequest;
import com.wissen.hotel.model.Amenity;
import com.wissen.hotel.repository.AmenityRepository;
import com.wissen.hotel.service.impl.AmenityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AmenityServiceImplTest {

    @Mock
    private AmenityRepository amenityRepository;

    @InjectMocks
    private AmenityServiceImpl amenityService;

    private AutoCloseable closeable;

    private Amenity sampleAmenity;
    private UUID amenityId;
    private CreateOrUpdateAmenityRequest request;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        amenityId = UUID.randomUUID();

        sampleAmenity = Amenity.builder()
                .amenityId(amenityId)
                .name("WiFi")
                .description("High-speed internet")
                .build();

        request = new CreateOrUpdateAmenityRequest();
        request.setName("WiFi");
        request.setDescription("High-speed internet");
    }

    @Test
    void testGetAllAmenities_Success() {
        List<Amenity> amenities = List.of(sampleAmenity);
        when(amenityRepository.findAll()).thenReturn(amenities);

        List<AmenityResponse> response = amenityService.getAllAmenities();

        assertEquals(1, response.size());
        assertEquals("WiFi", response.get(0).getName());
        verify(amenityRepository, times(1)).findAll();
    }

    @Test
    void testGetAmenityById_Success() {
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(sampleAmenity));

        AmenityResponse response = amenityService.getAmenityById(amenityId);

        assertNotNull(response);
        assertEquals("WiFi", response.getName());
        verify(amenityRepository).findById(amenityId);
    }

    @Test
    void testGetAmenityById_NotFound() {
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            amenityService.getAmenityById(amenityId);
        });

        assertEquals("Amenity not found", exception.getMessage());
    }

    @Test
    void testCreateAmenity_Success() {
        Amenity savedAmenity = Amenity.builder()
                .amenityId(amenityId)
                .name("WiFi")
                .description("High-speed internet")
                .build();

        when(amenityRepository.save(any(Amenity.class))).thenReturn(savedAmenity);

        AmenityResponse response = amenityService.createAmenity(request);

        assertNotNull(response);
        assertEquals("WiFi", response.getName());
        verify(amenityRepository).save(any(Amenity.class));
    }

    @Test
    void testUpdateAmenity_Success() {
        Amenity updatedAmenity = Amenity.builder()
                .amenityId(amenityId)
                .name("WiFi")
                .description("Updated description")
                .build();

        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(sampleAmenity));
        when(amenityRepository.save(any(Amenity.class))).thenReturn(updatedAmenity);

        request.setDescription("Updated description");
        AmenityResponse response = amenityService.updateAmenity(amenityId, request);

        assertNotNull(response);
        assertEquals("Updated description", response.getDescription());
        verify(amenityRepository).findById(amenityId);
        verify(amenityRepository).save(sampleAmenity);
    }

    @Test
    void testUpdateAmenity_NotFound() {
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            amenityService.updateAmenity(amenityId, request);
        });

        assertEquals("Amenity not found", exception.getMessage());
        verify(amenityRepository, never()).save(any());
    }

    @Test
    void testDeleteAmenity_Success() {
        when(amenityRepository.existsById(amenityId)).thenReturn(true);
        doNothing().when(amenityRepository).deleteById(amenityId);

        assertDoesNotThrow(() -> amenityService.deleteAmenity(amenityId));
        verify(amenityRepository).existsById(amenityId);
        verify(amenityRepository).deleteById(amenityId);
    }

    @Test
    void testDeleteAmenity_NotFound() {
        when(amenityRepository.existsById(amenityId)).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            amenityService.deleteAmenity(amenityId);
        });

        assertEquals("Amenity not found", exception.getMessage());
        verify(amenityRepository, never()).deleteById(any());
    }
}
