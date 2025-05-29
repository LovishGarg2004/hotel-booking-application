package com.wissen.hotel.services;

import com.wissen.hotel.dtos.ImageResponse;
import com.wissen.hotel.enums.ImageType;
import com.wissen.hotel.models.Image;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {
    private final CloudinaryService cloudinaryService;

    public ImageServiceImpl(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public Image uploadImage(UUID referenceId, ImageType type, MultipartFile file) throws IOException {
        return cloudinaryService.uploadImage(referenceId, type, file);
    }

    @Override
    public List<Image> getImagesByReference(UUID referenceId, ImageType type) {
        return cloudinaryService.getImagesByReference(referenceId, type);
    }

    @Override
    public List<Image> getImagesByType(ImageType type) {
        return cloudinaryService.getImagesByType(type);
    }

    @Override
    public void deleteImage(UUID imageId) throws IOException {
        cloudinaryService.deleteImage(imageId);
    }

    @Override
    public ImageResponse mapToImageResponse(Image image) {
        return cloudinaryService.mapToImageResponse(image);
    }
}
