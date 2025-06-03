// In src/main/java/com/wissen/hotel/service/CloudinaryService.java
package com.wissen.hotel.service;

import com.wissen.hotel.dto.response.ImageResponse;
import com.wissen.hotel.enums.ImageType;
import com.wissen.hotel.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface CloudinaryService {
    Image uploadImage(UUID referenceId, ImageType type, MultipartFile file) throws IOException;
    void deleteImage(UUID imageId) throws IOException;
    List<Image> getImagesByReference(UUID referenceId, ImageType type);
    List<Image> getImagesByType(ImageType type);
    ImageResponse mapToImageResponse(Image image);
}