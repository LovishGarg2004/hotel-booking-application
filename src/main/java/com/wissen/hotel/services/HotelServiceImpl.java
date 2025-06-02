package com.wissen.hotel.services;

import com.wissen.hotel.dtos.CreateHotelRequest;
import com.wissen.hotel.dtos.UpdateHotelRequest;
import com.wissen.hotel.dtos.HotelResponse;
import com.wissen.hotel.dtos.ReviewResponse;
import com.wissen.hotel.dtos.RoomResponse;
import com.wissen.hotel.models.Hotel;
import com.wissen.hotel.models.Room;
import com.wissen.hotel.repositories.HotelRepository;
import com.wissen.hotel.repositories.RoomAvailabilityRepository;
import com.wissen.hotel.repositories.RoomRepository;
import com.wissen.hotel.utils.AuthUtil;
import com.wissen.hotel.dtos.AmenityResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private static final Logger logger = LoggerFactory.getLogger(HotelServiceImpl.class);
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final RoomAvailabilityService roomAvailabilityService;
    private final ReviewService reviewService;
    private final PricingEngineService pricingEngineService;
    private static final String HOTEL_NOT_FOUND = "Hotel not found";

    private final EmailService emailService;

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
        var currentUser = AuthUtil.getCurrentUser();
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
                .owner(currentUser) // If current user logic available
                .build();
        Hotel saved = hotelRepository.save(hotel);
        logger.debug("Hotel created with ID: {}", saved.getHotelId());

        try {
            emailService.sendHotelRegistrationSuccessful(
                currentUser.getEmail(),
                saved.getName(),
                currentUser.getName()
            );
            emailService.sendAdminHotelRegistrationAlert(
                "admin@hotelbooking.com", // or fetch admin email from config/db
                saved.getName(),
                currentUser.getEmail()
            );
        } catch (Exception e) {
            logger.error("Failed to send registration emails: {}", e.getMessage(), e);
            // Optionally rethrow or handle as needed
        }
    
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

        try {
            emailService.sendHotelApprovalNotification(
                approved.getOwner().getEmail(),
                approved.getName()
            );
        } catch (Exception e) {
            logger.error("Failed to send approval email: {}", e.getMessage(), e);
            // Optionally rethrow or handle as needed
        }
        return mapToResponse(approved);
    }

    private HotelResponse 
    mapToResponse(Hotel hotel) {
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
    public List<HotelResponse> searchHotels(String city, LocalDate checkIn, LocalDate checkOut, int numberOfGuests, int page, int size) {
        return hotelRepository.findAll().stream()
            .filter(hotel -> (city == null || hotel.getCity().equalsIgnoreCase(city)))
            .map(hotel -> {
                List<Room> rooms = roomRepository.findAllByHotel_HotelId(hotel.getHotelId());
                // Sort rooms by capacity descending (largest rooms first)
                rooms = rooms.stream().sorted((a, b) -> Integer.compare(b.getCapacity(), a.getCapacity())).toList();

                BigDecimal minTotalPrice = null;
                int minRoomsRequired = 0;
                Room chosenRoom = null;

                for (Room room : rooms) {
                    int guestsLeft = numberOfGuests;
                    int roomsRequired = 0;
                    BigDecimal totalPrice = BigDecimal.ZERO;

                    // Calculate how many rooms needed for this room type
                    int needed = (int) Math.ceil((double) guestsLeft / room.getCapacity());
                    int maxAvailable = Integer.MAX_VALUE;

                    // Find the minimum available rooms for this room across the date range
                    for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
                        var availability = roomAvailabilityRepository.findByRoom_RoomIdAndDate(room.getRoomId(), date);
                        int available = (availability != null) ? availability.getAvailableRooms() : room.getTotalRooms();
                        if (available < maxAvailable) maxAvailable = available;
                    }

                    // If not enough rooms available, skip this room
                    if (maxAvailable < needed) continue;

                    // Calculate price for needed rooms
                    BigDecimal price = pricingEngineService.calculatePrice(room.getRoomId(), checkIn, checkOut).getFinalPrice();
                    totalPrice = price.multiply(BigDecimal.valueOf(needed));
                    roomsRequired = needed;

                    // If this is the first valid option or cheaper than previous, select it
                    if (minTotalPrice == null || totalPrice.compareTo(minTotalPrice) < 0) {
                        minTotalPrice = totalPrice;
                        minRoomsRequired = roomsRequired;
                        chosenRoom = room;
                    }
                }

                // If a suitable room was found, return the HotelResponse
                if (chosenRoom != null) {
                    HotelResponse resp = mapToResponse(hotel);
                    resp.setRoomsRequired(minRoomsRequired);
                    resp.setFinalPrice(minTotalPrice);
                    return resp;
                } else {
                    return null; // No suitable room found for this hotel
                }
            })
            .filter(Objects::nonNull)
            .skip((long) page * size)
            .limit(size)
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
    public List<RoomResponse> getHotelRooms(UUID hotelId) {
        logger.info("Fetching rooms for hotel ID: {}", hotelId);
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> {
                    logger.warn("Hotel not found for fetching rooms, ID: {}", hotelId);
                    return new RuntimeException(HOTEL_NOT_FOUND);
                });
        List<Room> rooms = roomRepository.findAllByHotel_HotelId(hotelId);
        return rooms.stream()
            .map(room -> RoomResponse.builder()
                .roomId(room.getRoomId())
                .hotelId(room.getHotel().getHotelId())
                .roomType(room.getRoomType())
                .capacity(room.getCapacity())
                .basePrice(room.getBasePrice())
                .totalRooms(room.getTotalRooms())
                .amenities(room.getRoomAmenities() != null ?
                    room.getRoomAmenities().stream().map(ra -> new AmenityResponse(
                        ra.getAmenity().getAmenityId(),
                        ra.getAmenity().getName(),
                        ra.getAmenity().getDescription()
                    )).toList() :
                    Collections.emptyList())
                .build())
            .toList();
    }

    @Override
    public Object checkAvailability(UUID hotelId, String checkIn, String checkOut) {
        logger.info("Checking available rooms for hotel ID: {} from {} to {}", hotelId, checkIn, checkOut);
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> {
                    logger.warn("Hotel not found for checking availability, ID: {}", hotelId);
                    return new RuntimeException(HOTEL_NOT_FOUND);
                });
        LocalDate checkInDate = LocalDate.parse(checkIn);
        LocalDate checkOutDate = LocalDate.parse(checkOut);
        List<Room> rooms = roomRepository.findAllByHotel_HotelId(hotelId);
        return rooms.stream()
            .filter(room -> roomAvailabilityService.isRoomAvailableForRange(room.getRoomId(), checkInDate, checkOutDate))
            .map(room -> RoomResponse.builder()
                .roomId(room.getRoomId())
                .hotelId(room.getHotel().getHotelId())
                .roomType(room.getRoomType())
                .capacity(room.getCapacity())
                .basePrice(room.getBasePrice())
                .totalRooms(room.getTotalRooms())
                .amenities(room.getRoomAmenities() != null ?
                    room.getRoomAmenities().stream().map(ra -> new AmenityResponse(
                        ra.getAmenity().getAmenityId(),
                        ra.getAmenity().getName(),
                        ra.getAmenity().getDescription()
                    )).toList() :
                    Collections.emptyList())
                .build())
            .toList();
    }

    @Override
    public List<HotelResponse> getHotelsOwnedByCurrentUser() {
        UUID currentUserId = AuthUtil.getCurrentUser().getUserId();
        return hotelRepository.findAll().stream()
                .filter(hotel -> hotel.getOwner() != null && hotel.getOwner().getUserId().equals(currentUserId))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public double getAverageRating(UUID hotelId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByHotel(hotelId);
        return reviews.isEmpty() ? 0.0 : reviews.stream().mapToInt(ReviewResponse::getRating).average().orElse(0.0);
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
