package com.wissen.hotel.service.impl;

import com.wissen.hotel.dto.request.*;
import com.wissen.hotel.dto.response.*;
import com.wissen.hotel.model.*;
import com.wissen.hotel.repository.*;
import com.wissen.hotel.service.EmailService;
import com.wissen.hotel.service.HotelService;
import com.wissen.hotel.service.PricingEngineService;
import com.wissen.hotel.service.ReviewService;
import com.wissen.hotel.service.RoomAvailabilityService;
import com.wissen.hotel.util.AuthUtil;

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
        return hotelRepository.findAll().stream()
            .filter(hotel -> hotel.isApproved()) // Only approved hotels
            .filter(hotel -> city == null || hotel.getCity().equalsIgnoreCase(city))
            .map(hotel -> {
                List<Room> rooms = roomRepository.findAllByHotel_HotelId(hotel.getHotelId());
                HotelResponse response = mapToResponse(hotel);
                BigDecimal minPrice = null;
                if (rooms != null && !rooms.isEmpty()) {
                minPrice = rooms.stream()
                    .map(Room::getBasePrice)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(null);
                }
                response.setFinalPrice(minPrice);
                response.setRoomsRequired(1); // Default to 1 room if no specific logic
                return response;
            })
            .skip((long) page * size)
            .limit(size)
            .toList();
    }

    @Override
    public HotelResponse getHotelById(UUID id) {
        return hotelRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException(HOTEL_NOT_FOUND));
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
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(HOTEL_NOT_FOUND));

        hotel.setName(request.getName());
        hotel.setAddress(request.getAddress());
        hotel.setDescription(request.getDescription());
        hotel.setCity(request.getCity());
        hotel.setState(request.getState());
        hotel.setCountry(request.getCountry());
        hotel.setLatitude(request.getLatitude());
        hotel.setLongitude(request.getLongitude());
        Hotel updated = hotelRepository.save(hotel);
        return mapToResponse(updated);
    }

    @Override
    public void deleteHotel(UUID id) {
        hotelRepository.deleteById(id);
    }

    @Override
    public HotelResponse approveHotel(UUID id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(HOTEL_NOT_FOUND));
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
    public List<HotelResponse> searchHotels(String city, LocalDate checkIn, LocalDate checkOut, int numberOfGuests, int page, int size) {
        return hotelRepository.findAll().stream()
            .filter(hotel -> hotel.isApproved()) // Only approved hotels
            .filter(hotel -> (city == null || hotel.getCity().equalsIgnoreCase(city)))
            .map(hotel -> {
                List<Room> rooms = roomRepository.findAllByHotel_HotelId(hotel.getHotelId());
                if (rooms == null || rooms.isEmpty()) {
                    return null; // Exclude hotels with no rooms
                }
                rooms = rooms.stream().sorted((a, b) -> Integer.compare(b.getCapacity(), a.getCapacity())).toList();

                BigDecimal minTotalPrice = null;
                int minRoomsRequired = 0;
                Room chosenRoom = null;

                for (Room room : rooms) {
                    int guestsLeft = numberOfGuests;
                    int roomsRequired = 0;
                    BigDecimal totalPrice = BigDecimal.ZERO;

                    int needed = (int) Math.ceil((double) guestsLeft / room.getCapacity());
                    int maxAvailable = Integer.MAX_VALUE;

                    for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
                        var availability = roomAvailabilityRepository.findByRoom_RoomIdAndDate(room.getRoomId(), date);
                        int available = (availability != null) ? availability.getAvailableRooms() : room.getTotalRooms();
                        if (available < maxAvailable) maxAvailable = available;
                    }
                    if (maxAvailable < needed) continue;

                    BigDecimal price = pricingEngineService.calculatePrice(room.getRoomId(), checkIn, checkOut).getFinalPrice();
                    totalPrice = price.multiply(BigDecimal.valueOf(needed));
                    roomsRequired = needed;

                    if (minTotalPrice == null || totalPrice.compareTo(minTotalPrice) < 0) {
                        minTotalPrice = totalPrice;
                        minRoomsRequired = roomsRequired;
                        chosenRoom = room;
                    }
                }
                if (chosenRoom != null) {
                    HotelResponse resp = mapToResponse(hotel);
                    resp.setRoomsRequired(minRoomsRequired);
                    resp.setFinalPrice(minTotalPrice);
                    return resp;
                } else {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .skip((long) page * size)
            .limit(size)
            .toList();
    }

    @Override
    public List<HotelResponse> getTopRatedHotels() {
        return hotelRepository.findAll().stream()
                .sorted((h1, h2) -> Double.compare(
                        getAverageRating(h2.getHotelId()),
                        getAverageRating(h1.getHotelId())
                ))
                .map(hotel -> {
                    List<Room> rooms = roomRepository.findAllByHotel_HotelId(hotel.getHotelId());
                    if (rooms == null || rooms.isEmpty()) {
                        return null; // Exclude hotels with no rooms
                    }
                    HotelResponse response = mapToResponse(hotel);
                    BigDecimal minPrice = rooms.stream()
                            .map(Room::getBasePrice)
                            .filter(Objects::nonNull)
                            .min(BigDecimal::compareTo)
                            .orElse(null);
                    response.setFinalPrice(minPrice);
                    response.setRoomsRequired(1);
                    return response;
                })
                .filter(Objects::nonNull)
                .limit(10)
                .toList();
    }

    @Override
    public List<HotelResponse> findNearbyHotels(double latitude, double longitude, double radiusKm) {
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
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException(HOTEL_NOT_FOUND));
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
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException(HOTEL_NOT_FOUND));
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

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371;
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
