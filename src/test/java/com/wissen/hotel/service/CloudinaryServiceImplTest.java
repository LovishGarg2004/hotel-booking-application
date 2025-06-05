package com.wissen.hotel.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import com.wissen.hotel.dto.response.ImageResponse;
import com.wissen.hotel.enums.ImageType;
import com.wissen.hotel.exception.ResourceNotFoundException;
import com.wissen.hotel.model.Image;
import com.wissen.hotel.repository.ImageRepository;
import com.wissen.hotel.service.impl.CloudinaryServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CloudinaryServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryServiceImpl cloudinaryService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(cloudinary.uploader()).thenReturn(uploader);
        cloudinaryService = new CloudinaryServiceImpl(cloudinary, imageRepository);

        // Fix: allow access to private field
        java.lang.reflect.Field field = cloudinaryService.getClass().getDeclaredField("baseFolder");
        field.setAccessible(true);
        field.set(cloudinaryService, "hotel-booking");
    }


    @Test
    void testUploadImage_Success() throws Exception {
        UUID referenceId = UUID.randomUUID();
        ImageType type = ImageType.HOTEL;
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy-image".getBytes());

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "https://res.cloudinary.com/demo/image/upload/v123456/hotel/hotelId/image.jpg");

        when(uploader.upload(any(), anyMap())).thenReturn(uploadResult);
        when(imageRepository.save(any(Image.class))).thenAnswer(i -> i.getArguments()[0]);

        Image savedImage = cloudinaryService.uploadImage(referenceId, type, file);

        assertEquals(referenceId, savedImage.getReferenceId());
        assertEquals(type, savedImage.getType());
        assertNotNull(savedImage.getImageUrl());
    }

    @Test
    void testUploadImage_Failure() throws Exception {
        UUID referenceId = UUID.randomUUID();
        ImageType type = ImageType.HOTEL;
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy-image".getBytes());

        when(uploader.upload(any(), anyMap())).thenThrow(new IOException("Upload failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            cloudinaryService.uploadImage(referenceId, type, file)
        );
        assertTrue(exception.getMessage().contains("Failed to upload image"));
    }

    @Test
    void testDeleteImage_Success() throws Exception {
        UUID imageId = UUID.randomUUID();
        Image image = Image.builder()
            .imageId(imageId)
            .imageUrl("https://res.cloudinary.com/demo/image/upload/v123456/hotel/hotelId/image.jpg")
            .build();

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(cloudinary.uploader()).thenReturn(uploader);

        assertDoesNotThrow(() -> cloudinaryService.deleteImage(imageId));
        verify(imageRepository).delete(image);
    }

    @Test
    void testDeleteImage_ResourceNotFound() {
        UUID imageId = UUID.randomUUID();
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> cloudinaryService.deleteImage(imageId));
    }

    @Test
    void testDeleteImage_FailureOnDelete() throws Exception {
        UUID imageId = UUID.randomUUID();
        Image image = Image.builder()
            .imageId(imageId)
            .imageUrl("https://res.cloudinary.com/demo/image/upload/v123456/hotel/hotelId/image.jpg")
            .build();

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));
        doThrow(new RuntimeException("Failed")).when(imageRepository).delete(any());

        assertThrows(RuntimeException.class, () -> cloudinaryService.deleteImage(imageId));
    }

    @Test
    void testGetImagesByReference_WithType() {
        UUID referenceId = UUID.randomUUID();
        ImageType type = ImageType.ROOM;

        List<Image> images = Collections.singletonList(Image.builder().referenceId(referenceId).type(type).build());
        when(imageRepository.findByReferenceIdAndType(referenceId, type)).thenReturn(images);

        List<Image> result = cloudinaryService.getImagesByReference(referenceId, type);
        assertEquals(1, result.size());
    }

    @Test
    void testGetImagesByReference_WithoutType() {
        UUID referenceId = UUID.randomUUID();
        List<Image> images = Collections.singletonList(Image.builder().referenceId(referenceId).build());
        when(imageRepository.findByReferenceId(referenceId)).thenReturn(images);

        List<Image> result = cloudinaryService.getImagesByReference(referenceId, null);
        assertEquals(1, result.size());
    }

    @Test
    void testGetImagesByType_Success() {
        ImageType type = ImageType.ROOM;
        when(imageRepository.findByType(type)).thenReturn(Collections.singletonList(Image.builder().type(type).build()));

        List<Image> images = cloudinaryService.getImagesByType(type);
        assertEquals(1, images.size());
    }

    @Test
    void testMapToImageResponse() {
        UUID id = UUID.randomUUID();
        Image image = Image.builder()
            .imageId(id)
            .imageUrl("http://example.com")
            .type(ImageType.HOTEL)
            .referenceId(UUID.randomUUID())
            .uploadedAt(LocalDateTime.now())
            .build();

        ImageResponse response = cloudinaryService.mapToImageResponse(image);
        assertEquals(id, response.getImageId());
        assertEquals("http://example.com", response.getImageUrl());
    }

    @Test
    void testExtractPublicId_ValidUrl() {
        String url = "https://res.cloudinary.com/demo/image/upload/v123456/hotel/hotelId/image.jpg";
        String publicId = cloudinaryService.extractPublicId(url);
        assertEquals("hotel/hotelId/image", publicId);
    }

    @Test
    void testExtractPublicId_InvalidUrl() {
        String url = "https://res.cloudinary.com/demo/image/no_upload_path.jpg";
        String publicId = cloudinaryService.extractPublicId(url);
        assertNull(publicId);
    }
}
