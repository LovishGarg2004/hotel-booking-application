package com.wissen.hotel.services;

import com.wissen.hotel.models.Image;
import com.wissen.hotel.repositories.HotelRepository;
import com.wissen.hotel.repositories.ImageRepository;
import com.wissen.hotel.repositories.RoomRepository;
import com.wissen.hotel.enums.ImageType;  // Add this import
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityService {
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ImageRepository imageRepository;

    /**
     * Check if the current user is the owner of a hotel
     */
    public boolean isHotelOwner(UUID hotelId) {
        String username = getCurrentUserUsername();
        if (username == null) return false;
        
        return hotelRepository.existsByHotelIdAndOwnerEmail(hotelId, username);
    }

    /**
     * Check if the current user is the owner of a room
     */
    public boolean isRoomOwner(UUID roomId) {
        String username = getCurrentUserUsername();
        if (username == null) return false;
        
        return roomRepository.existsByRoomIdAndHotelOwnerEmail(roomId, username);
    }

    /**
     * Check if the current user is the owner of an image
     */
    public boolean isImageOwner(UUID imageId) {
        String username = getCurrentUserUsername();
        if (username == null) return false;
        
        return imageRepository.findById(imageId)
            .map(image -> {
                if (image.getType() == ImageType.HOTEL) {
                    return hotelRepository.existsByIdAndOwnerEmail(image.getReferenceId(), username);
                } else if (image.getType() == ImageType.ROOM) {
                    return roomRepository.existsByRoomIdAndHotel_Owner_Email(image.getReferenceId(), username);
                }
                return false;
            })
            .orElse(false);
    }

    /**
     * Get the username of the currently authenticated user
     */
    private String getCurrentUserUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
}