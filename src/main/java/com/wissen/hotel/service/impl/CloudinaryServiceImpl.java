package com.wissen.hotel.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.wissen.hotel.dto.response.ImageResponse;
import com.wissen.hotel.enums.ImageType;
import com.wissen.hotel.exception.ResourceNotFoundException;
import com.wissen.hotel.model.Image;
import com.wissen.hotel.repository.ImageRepository;
import com.wissen.hotel.service.CloudinaryService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;
    private final ImageRepository imageRepository;

    @Value("${cloudinary.folder:hotel-booking}")
    private String baseFolder;

    @Override
    public Image uploadImage(UUID referenceId, ImageType type, MultipartFile file) {
        try {
            String folderPath = String.format("%s/%s/%s", 
                baseFolder, 
                type.name().toLowerCase(), 
                referenceId);
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "folder", folderPath,
                    "resource_type", "auto"
                )
            );
            Image image = Image.builder()
                .type(type)
                .referenceId(referenceId)
                .imageUrl((String) uploadResult.get("url"))
                .uploadedAt(LocalDateTime.now())
                .build();
            return imageRepository.save(image);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image. Please try again later.", e);
        }
    }

    @Override
    public void deleteImage(UUID imageId) {
        try {
            Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
            String publicId = extractPublicId(image.getImageUrl());
            if (publicId != null) {
                try {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                } catch (Exception e) {
                    System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
                }
            }
            imageRepository.delete(image);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image. Please try again later.", e);
        }
    }

    @Override
    public List<Image> getImagesByReference(UUID referenceId, ImageType type) {
        try {
            if (type != null) {
                return imageRepository.findByReferenceIdAndType(referenceId, type);
            }
            return imageRepository.findByReferenceId(referenceId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve images. Please try again later.", e);
        }
    }

    @Override
    public List<Image> getImagesByType(ImageType type) {
        try {
            return imageRepository.findByType(type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve images by type. Please try again later.", e);
        }
    }

    @Override
    public ImageResponse mapToImageResponse(Image image) {
        return ImageResponse.builder()
            .imageId(image.getImageId())
            .imageUrl(image.getImageUrl())
            .type(image.getType())
            .referenceId(image.getReferenceId())
            .uploadedAt(image.getUploadedAt())
            .build();
    }

    private String extractPublicId(String imageUrl) {
        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length > 1) {
                String path = parts[1];
                path = path.replaceFirst("v\\d+/", "");
                return path.substring(0, path.lastIndexOf('.'));
            }
        } catch (Exception e) {
            System.err.println("Error extracting public ID from URL: " + e.getMessage());
        }
        return null;
    }
}
