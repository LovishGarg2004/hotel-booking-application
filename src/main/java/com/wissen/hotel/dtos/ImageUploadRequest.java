package com.wissen.hotel.dtos;

import com.wissen.hotel.enums.ImageType;
import lombok.Data;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageUploadRequest {
    private UUID referenceId;
    private ImageType type;
    private MultipartFile file;
}