package com.wissen.hotel.services;

import com.wissen.hotel.dtos.CreateHotelRequest;
import com.wissen.hotel.dtos.UpdateHotelRequest;
import com.wissen.hotel.dtos.HotelResponse;
import com.wissen.hotel.models.Hotel;
import com.wissen.hotel.repositories.HotelRepository;
import com.wissen.hotel.utils.AuthUtil;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private static final Logger logger = LoggerFactory.getLogger(HotelServiceImpl.class);
    private final HotelRepository hotelRepository;
    private static final String HOTEL_NOT_FOUND = "Hotel not found";

    @Override
    public List<HotelResponse> getAllHotels(String city, int page, int size) {
        logger.info("Fetching all hotels. City: {}, Page: {}, Size: {}", city, page, size);
        List<HotelResponse> hotels = hotelRepository.findAll().stream()
                .filter(hotel -> city == null || hotel.getCity().equalsIgnoreCase(city))
                .skip((long) page * size)
                .limit(size)
                .map(this::mapToResponse)
                .toList();
        logger.debug("Found {} hotels", hotels.size());
        return hotels;
    }

    @Override
    public HotelResponse getHotelById(UUID id) {
        logger.info("Fetching hotel by ID: {}", id);
        return hotelRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> {
                    logger.warn("Hotel not found for ID: {}", id);
                    return new RuntimeException(HOTEL_NOT_FOUND);
                });
    }

    @Override
    public HotelResponse createHotel(CreateHotelRequest request) {
        logger.info("Creating hotel: {}", request.getName());
        Hotel hotel = Hotel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .createdAt(LocalDateTime.now())
                .isApproved(false)
                .owner(AuthUtil.getCurrentUser()) // If current user logic available
                .build();
        Hotel saved = hotelRepository.save(hotel);
        logger.debug("Hotel created with ID: {}", saved.getHotelId());
        return mapToResponse(saved);
    }


    @Override
    public HotelResponse updateHotel(UUID id, UpdateHotelRequest request) {
        logger.info("Updating hotel ID: {}", id);
        Hotel hotel = hotelRepository.findById(id)

                .orElseThrow(() -> {
                    logger.warn("Hotel not found for update, ID: {}", id);
                    return new RuntimeException(HOTEL_NOT_FOUND);
                });

        hotel.setName(request.getName());
        hotel.setAddress(request.getAddress());
        hotel.setDescription(request.getDescription());
        hotel.setCity(request.getCity());
        hotel.setState(request.getState());
        hotel.setCountry(request.getCountry());
        hotel.setLatitude(request.getLatitude());
        hotel.setLongitude(request.getLongitude());
        Hotel updated = hotelRepository.save(hotel);
        logger.debug("Hotel updated: {}", updated.getHotelId());
        return mapToResponse(updated);
    }

    @Override
    public void deleteHotel(UUID id) {
        logger.info("Deleting hotel ID: {}", id);
        hotelRepository.deleteById(id);
        logger.debug("Hotel deleted: {}", id);
    }

    @Override
    public HotelResponse approveHotel(UUID id) {
        logger.info("Approving hotel ID: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Hotel not found for approval, ID: {}", id);
                    return new RuntimeException(HOTEL_NOT_FOUND);
                });

        hotel.setApproved(true);
        Hotel approved = hotelRepository.save(hotel);
        logger.debug("Hotel approved: {}", approved.getHotelId());
        return mapToResponse(approved);
    }

    private HotelResponse mapToResponse(Hotel hotel) {
        return HotelResponse.builder()
                .hotelId(hotel.getHotelId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .address(hotel.getAddress())
                .city(hotel.getCity())
                .state(hotel.getState())
                .country(hotel.getCountry())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .isApproved(hotel.isApproved())
                .createdAt(hotel.getCreatedAt())
                .ownerId(hotel.getOwner().getUserId())
                .build();
    }

    @Override
    public List<HotelResponse> searchHotels(String keyword, String city, int page, int size) {
        return hotelRepository.findAll().stream()
                .filter(h -> (keyword == null || h.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                            h.getDescription().toLowerCase().contains(keyword.toLowerCase())) &&
                            (city == null || h.getCity().equalsIgnoreCase(city)))
                .skip((long) page * size)
                .limit(size)
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<HotelResponse> getTopRatedHotels() {
        // Stub: Ideally compute average rating from ReviewRepository
        return hotelRepository.findAll().stream()
                .limit(10) // temporary, you can sort by rating when added
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<HotelResponse> findNearbyHotels(double latitude, double longitude, double radiusKm) {
        // Stub: Use haversine formula for geo-calculation in real use
        return hotelRepository.findAll().stream()
                .filter(hotel -> {
                    if (hotel.getLatitude() == null || hotel.getLongitude() == null) return false;
                    double dist = distance(latitude, longitude, hotel.getLatitude().doubleValue(), hotel.getLongitude().doubleValue());
                    return dist <= radiusKm;
                })
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public Object getHotelRooms(UUID hotelId) {
        // Stubbed response
        return "List of rooms for hotel " + hotelId;
    }

    @Override
    public Object getHotelReviews(UUID hotelId) {
        // Stubbed response
        return "List of reviews for hotel " + hotelId;
    }

    @Override
    public Object checkAvailability(UUID hotelId, String checkIn, String checkOut) {
        // Stubbed response
        return "Available rooms at hotel " + hotelId + " from " + checkIn + " to " + checkOut;
    }

    @Override
    public List<HotelResponse> getHotelsOwnedByCurrentUser() {
        // Stubbed current user logic
        UUID dummyUserId = UUID.randomUUID(); // Replace with actual logged-in user ID
        return hotelRepository.findAll().stream()
                .filter(hotel -> hotel.getOwner() != null && hotel.getOwner().getUserId().equals(dummyUserId))
                .map(this::mapToResponse)
                .toList();
    }

    // Helper to calculate distance (Haversine)
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lon2 - lon1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
}

    
}
