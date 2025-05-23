package com.wissen.hotel.dtos;

import com.wissen.hotel.enums.ImageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ImageResponse {
    private UUID imageId;
    private String imageUrl;
    private ImageType type;
    private UUID referenceId;
    private LocalDateTime uploadedAt;
}