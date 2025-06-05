package com.wissen.hotel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.wissen.hotel.dto.response.ImageResponse;
import com.wissen.hotel.enums.ImageType;
import com.wissen.hotel.model.Image;
import com.wissen.hotel.service.CloudinaryService;
import com.wissen.hotel.service.impl.ImageServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @Mock
    private CloudinaryService cloudinaryService;
    
    @Mock
    private MultipartFile multipartFile;
    
    @InjectMocks
    private ImageServiceImpl imageService;
    
    private UUID referenceId;
    private UUID imageId;
    private Image image;
    private ImageResponse imageResponse;

    @BeforeEach
    void setUp() {
        referenceId = UUID.randomUUID();
        imageId = UUID.randomUUID();
        image = new Image();
        image.setImageId(imageId);

        imageResponse = ImageResponse.builder()
            .imageId(imageId)
            .referenceId(referenceId)
            .imageUrl("http://example.com/image.jpg")
            .type(ImageType.USER)
            .build();
    }

    // Upload Image Tests
    @ParameterizedTest
    @EnumSource(ImageType.class)
    void uploadImage_ShouldReturnImage_ForAllImageTypes(ImageType type) throws Exception {
        when(cloudinaryService.uploadImage(any(), any(), any())).thenReturn(image);
        
        Image result = imageService.uploadImage(referenceId, type, multipartFile);
        
        assertEquals(imageId, result.getImageId());
        verify(cloudinaryService).uploadImage(referenceId, type, multipartFile);
    }

    @Test
    void uploadImage_ShouldWrapCheckedExceptions() throws Exception {
        IOException ioException = new IOException("Cloudinary connection failed");
        when(cloudinaryService.uploadImage(any(), any(), any())).thenThrow(ioException);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            imageService.uploadImage(referenceId, ImageType.USER, multipartFile)
        );

        assertEquals("Failed to upload image. Please try again later.", exception.getMessage());
        assertEquals(ioException, exception.getCause());
    }

    // Get Images Tests
    @Test
    void getImagesByReference_ShouldHandleEmptyList() throws Exception {
        when(cloudinaryService.getImagesByReference(any(), any())).thenReturn(Collections.emptyList());
        
        List<Image> result = imageService.getImagesByReference(referenceId, ImageType.ROOM);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void getImagesByReference_ShouldPreserveExceptionCause() throws Exception {
        IllegalStateException originalEx = new IllegalStateException("Invalid state");
        when(cloudinaryService.getImagesByReference(any(), any())).thenThrow(originalEx);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            imageService.getImagesByReference(referenceId, ImageType.ROOM)
        );

        assertEquals(originalEx, exception.getCause());
    }

    // Delete Image Tests
    @Test
    void deleteImage_ShouldVerifyExceptionMessageStructure() throws Exception {
        doThrow(new IOException("Storage error")).when(cloudinaryService).deleteImage(any());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            imageService.deleteImage(imageId)
        );

        assertEquals("Failed to delete image. Please try again later.", exception.getMessage());
        verify(cloudinaryService).deleteImage(imageId);
    }


    // Response Mapping Tests
    @Test
    void mapToImageResponse_ShouldReturnNull_WhenCloudinaryServiceReturnsNull() throws Exception {
        when(cloudinaryService.mapToImageResponse(any())).thenReturn(null);
        
        ImageResponse result = imageService.mapToImageResponse(image);
        
        assertNull(result);
    }

    @Test
    void mapToImageResponse_ShouldHandleEmptyImage() throws Exception {
        Image emptyImage = new Image();
        when(cloudinaryService.mapToImageResponse(any())).thenReturn(imageResponse);
        
        ImageResponse result = imageService.mapToImageResponse(emptyImage);
        
        assertSame(imageResponse, result);
    }

    // Edge Case Tests
    @Test
    void allMethods_ShouldHandleNullParameters() throws Exception {
        when(cloudinaryService.uploadImage(isNull(), any(), any())).thenThrow(new NullPointerException());
        when(cloudinaryService.getImagesByReference(isNull(), any())).thenThrow(new NullPointerException());
        when(cloudinaryService.getImagesByType(isNull())).thenThrow(new NullPointerException());
        when(cloudinaryService.mapToImageResponse(isNull())).thenThrow(new NullPointerException());
        doThrow(new NullPointerException()).when(cloudinaryService).deleteImage(isNull());

        assertAll(
            () -> assertThrows(RuntimeException.class, 
                () -> imageService.uploadImage(null, ImageType.USER, multipartFile)),
            () -> assertThrows(RuntimeException.class, 
                () -> imageService.getImagesByReference(null, ImageType.ROOM)),
            () -> assertThrows(RuntimeException.class, 
                () -> imageService.getImagesByType(null)),
            () -> assertThrows(RuntimeException.class, 
                () -> imageService.deleteImage(null)),
            () -> assertThrows(RuntimeException.class, 
                () -> imageService.mapToImageResponse(null))
        );
    }   

}
