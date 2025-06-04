package com.wissen.hotel.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.wissen.hotel.dto.response.ImageResponse;
import com.wissen.hotel.enums.ImageType;
import com.wissen.hotel.model.Image;
import com.wissen.hotel.service.ImageService;

class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    private UUID hotelId;
    private UUID roomId;
    private UUID imageId;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        hotelId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        imageId = UUID.randomUUID();
        mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
    }

    @Test
    void testUploadImage() throws IOException {
        ImageType type = ImageType.HOTEL;
        Image mockImage = mock(Image.class);
        ImageResponse mockResponse = mock(ImageResponse.class);
        
        when(imageService.uploadImage(hotelId, type, mockFile)).thenReturn(mockImage);
        when(imageService.mapToImageResponse(mockImage)).thenReturn(mockResponse);

        ResponseEntity<ImageResponse> response = imageController.uploadImage(
            mockFile, hotelId, type
        );

        assertEquals(mockResponse, response.getBody());
        verify(imageService).uploadImage(hotelId, type, mockFile);
    }

    @Test
    void testUploadHotelImage() throws IOException {
        Image mockImage = mock(Image.class);
        ImageResponse mockResponse = mock(ImageResponse.class);
        
        when(imageService.uploadImage(hotelId, ImageType.HOTEL, mockFile)).thenReturn(mockImage);
        when(imageService.mapToImageResponse(mockImage)).thenReturn(mockResponse);

        ResponseEntity<ImageResponse> response = imageController.uploadHotelImage(
            hotelId, mockFile
        );

        assertEquals(mockResponse, response.getBody());
        verify(imageService).uploadImage(hotelId, ImageType.HOTEL, mockFile);
    }

    @Test
    void testUploadRoomImage() throws IOException {
        Image mockImage = mock(Image.class);
        ImageResponse mockResponse = mock(ImageResponse.class);
        
        when(imageService.uploadImage(roomId, ImageType.ROOM, mockFile)).thenReturn(mockImage);
        when(imageService.mapToImageResponse(mockImage)).thenReturn(mockResponse);

        ResponseEntity<ImageResponse> response = imageController.uploadRoomImage(
            roomId, mockFile
        );

        assertEquals(mockResponse, response.getBody());
        verify(imageService).uploadImage(roomId, ImageType.ROOM, mockFile);
    }

    @Test
    void testGetHotelImages() {
        List<Image> mockImages = Arrays.asList(mock(Image.class), mock(Image.class));
        List<ImageResponse> mockResponses = Arrays.asList(mock(ImageResponse.class), mock(ImageResponse.class));
        
        when(imageService.getImagesByReference(hotelId, ImageType.HOTEL)).thenReturn(mockImages);
        when(imageService.mapToImageResponse(any(Image.class))).thenReturn(mockResponses.get(0), mockResponses.get(1));

        ResponseEntity<List<ImageResponse>> response = imageController.getHotelImages(hotelId);
        
        assertEquals(2, response.getBody().size());
        verify(imageService).getImagesByReference(hotelId, ImageType.HOTEL);
    }

    @Test
    void testGetRoomImages() {
        List<Image> mockImages = Collections.singletonList(mock(Image.class));
        ImageResponse mockResponse = mock(ImageResponse.class);
        
        when(imageService.getImagesByReference(roomId, ImageType.ROOM)).thenReturn(mockImages);
        when(imageService.mapToImageResponse(any(Image.class))).thenReturn(mockResponse);

        ResponseEntity<List<ImageResponse>> response = imageController.getRoomImages(roomId);
        
        assertEquals(1, response.getBody().size());
        verify(imageService).getImagesByReference(roomId, ImageType.ROOM);
    }

    @Test
    void testGetImagesByReferenceWithType() {
        ImageType type = ImageType.HOTEL;
        List<Image> mockImages = Arrays.asList(mock(Image.class), mock(Image.class));
        
        when(imageService.getImagesByReference(hotelId, type)).thenReturn(mockImages);
        when(imageService.mapToImageResponse(any(Image.class))).thenReturn(mock(ImageResponse.class));

        ResponseEntity<List<ImageResponse>> response = imageController.getImagesByReference(hotelId, type);
        
        assertEquals(2, response.getBody().size());
        verify(imageService).getImagesByReference(hotelId, type);
    }

    @Test
    void testGetImagesByReferenceWithoutType() {
        List<Image> mockImages = Collections.singletonList(mock(Image.class));
        
        when(imageService.getImagesByReference(hotelId, null)).thenReturn(mockImages);
        when(imageService.mapToImageResponse(any(Image.class))).thenReturn(mock(ImageResponse.class));

        ResponseEntity<List<ImageResponse>> response = imageController.getImagesByReference(hotelId, null);
        
        assertEquals(1, response.getBody().size());
        verify(imageService).getImagesByReference(hotelId, null);
    }

    @Test
    void testGetImagesByType() {
        ImageType type = ImageType.ROOM;
        List<Image> mockImages = Arrays.asList(mock(Image.class), mock(Image.class));
        
        when(imageService.getImagesByType(type)).thenReturn(mockImages);
        when(imageService.mapToImageResponse(any(Image.class))).thenReturn(mock(ImageResponse.class));

        ResponseEntity<List<ImageResponse>> response = imageController.getImagesByType(type);
        
        assertEquals(2, response.getBody().size());
        verify(imageService).getImagesByType(type);
    }

    @Test
    void testDeleteImage() throws IOException {
        ResponseEntity<Void> response = imageController.deleteImage(imageId);
        
        assertEquals(204, response.getStatusCodeValue());
        verify(imageService).deleteImage(imageId);
    }
}
