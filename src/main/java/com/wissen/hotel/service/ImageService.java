package com.wissen.hotel.service;

import com.wissen.hotel.dto.response.ImageResponse;
import com.wissen.hotel.enums.ImageType;
import com.wissen.hotel.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ImageService {
    Image uploadImage(UUID referenceId, ImageType type, MultipartFile file) throws IOException;
    List<Image> getImagesByReference(UUID referenceId, ImageType type);
    List<Image> getImagesByType(ImageType type);
    void deleteImage(UUID imageId) throws IOException;
    ImageResponse mapToImageResponse(Image image);
}
