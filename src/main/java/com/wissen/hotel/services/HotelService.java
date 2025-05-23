package com.wissen.hotel.services;

import com.wissen.hotel.dtos.CreateHotelRequest;
import com.wissen.hotel.dtos.UpdateHotelRequest;
import com.wissen.hotel.dtos.HotelResponse;

import java.util.List;
import java.util.UUID;

public interface HotelService {
    List<HotelResponse> getAllHotels(String city, int page, int size);
    HotelResponse getHotelById(UUID id);
    HotelResponse createHotel(CreateHotelRequest request);
    HotelResponse updateHotel(UUID id, UpdateHotelRequest request);
    void deleteHotel(UUID id);
    HotelResponse approveHotel(UUID id);

    List<HotelResponse> searchHotels(String keyword, String city, int page, int size);
    List<HotelResponse> getTopRatedHotels();
    List<HotelResponse> findNearbyHotels(double latitude, double longitude, double radiusKm);
    Object getHotelRooms(UUID hotelId);
    Object getHotelReviews(UUID hotelId);
    Object checkAvailability(UUID hotelId, String checkIn, String checkOut);
    List<HotelResponse> getHotelsOwnedByCurrentUser();
    
    List<HotelResponse> getFeaturedHotels();

}
