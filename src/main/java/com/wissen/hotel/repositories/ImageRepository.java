package com.wissen.hotel.repositories;

import com.wissen.hotel.enums.ImageType;
import com.wissen.hotel.models.Image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {
    List<Image> findByReferenceId(UUID referenceId);
    List<Image> findByReferenceIdAndType(UUID referenceId, ImageType type);
    List<Image> findByType(ImageType type);

}