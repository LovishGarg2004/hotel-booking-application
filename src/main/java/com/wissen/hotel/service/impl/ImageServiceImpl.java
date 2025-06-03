package com.wissen.hotel.service.impl;

import com.wissen.hotel.dto.response.ImageResponse;
import com.wissen.hotel.enums.ImageType;
import com.wissen.hotel.model.Image;
import com.wissen.hotel.service.CloudinaryService;
import com.wissen.hotel.service.ImageService;

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
    public Image uploadImage(UUID referenceId, ImageType type, MultipartFile file) {
        try {
            return cloudinaryService.uploadImage(referenceId, type, file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image. Please try again later.", e);
        }
    }

    @Override
    public List<Image> getImagesByReference(UUID referenceId, ImageType type) {
        try {
            return cloudinaryService.getImagesByReference(referenceId, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve images. Please try again later.", e);
        }
    }

    @Override
    public List<Image> getImagesByType(ImageType type) {
        try {
            return cloudinaryService.getImagesByType(type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve images by type. Please try again later.", e);
        }
    }

    @Override
    public void deleteImage(UUID imageId) {
        try {
            cloudinaryService.deleteImage(imageId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image. Please try again later.", e);
        }
    }

    @Override
    public ImageResponse mapToImageResponse(Image image) {
        try {
            return cloudinaryService.mapToImageResponse(image);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map image to response. Please try again later.", e);
        }
    }
}
