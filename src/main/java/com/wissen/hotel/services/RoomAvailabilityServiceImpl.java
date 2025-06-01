package com.wissen.hotel.services;

import com.wissen.hotel.dtos.UpdateInventoryRequest;
import com.wissen.hotel.dtos.BlockRoomRequest;
import com.wissen.hotel.models.Room;
import com.wissen.hotel.models.RoomAvailability;
import com.wissen.hotel.repositories.RoomAvailabilityRepository;
import com.wissen.hotel.repositories.RoomRepository;
import com.wissen.hotel.exceptions.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityServiceImpl implements RoomAvailabilityService {

    private final RoomAvailabilityRepository availabilityRepository;
    private final RoomRepository roomRepository;

    @Override
    public boolean isRoomAvailable(UUID roomId, LocalDate date) {
        try {
            RoomAvailability availability = availabilityRepository.findByRoom_RoomIdAndDate(roomId, date);
            if (availability == null) {
                return true; // If no availability record exists, assume room is available
            }
            return availability.getAvailableRooms() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check room availability. Please try again later.", e);
        }
    }

    @Override
    public boolean isRoomAvailableForRange(UUID roomId, LocalDate checkin, LocalDate checkout) {
        try {
            // Check every date in the range [checkin, checkout) for availability
            for (LocalDate date = checkin; date.isBefore(checkout); date = date.plusDays(1)) {
                if (!isRoomAvailable(roomId, date)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check room availability for the range. Please try again later.", e);
        }
    }

    @Override
    public void updateInventory(UUID roomId, UpdateInventoryRequest request) {
        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
            RoomAvailability availability = availabilityRepository.findByRoom_RoomIdAndDate(roomId, request.getDate());

            int roomsToBook = request.getRoomsToBook();
            if (availability == null) {
                int availableRooms = room.getTotalRooms() - roomsToBook;
                if (roomsToBook > room.getTotalRooms()) {
                    throw new IllegalArgumentException("Rooms to book cannot exceed total rooms.");
                }
                availability = RoomAvailability.builder()
                    .room(room)
                    .date(request.getDate())
                    .availableRooms(availableRooms)
                    .build();
            } else {
                if (roomsToBook > availability.getAvailableRooms()) {
                    throw new IllegalArgumentException("Rooms to book cannot exceed available rooms.");
                }
                availability.setAvailableRooms(availability.getAvailableRooms() - roomsToBook);
            }

            availabilityRepository.save(availability);
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update inventory. Please try again later.", e);
        }
    }

    @Override
    public void blockRoomDates(UUID roomId, BlockRoomRequest request) {
        try {
            Room room = roomRepository.findById(roomId).orElseThrow();
            for (LocalDate date = request.getStartDate(); !date.isAfter(request.getEndDate()); date = date.plusDays(1)) {
                RoomAvailability availability = availabilityRepository.findByRoom_RoomIdAndDate(roomId, date);
                if (availability == null) {
                    availability = RoomAvailability.builder()
                            .room(room)
                            .date(date)
                            .availableRooms(0)
                            .build();
                } else {
                    availability.setAvailableRooms(0);
                }
                availabilityRepository.save(availability);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to block room dates. Please try again later.", e);
        }
    }

    @Override
    public void unblockRoomDates(UUID roomId, BlockRoomRequest request) {
        try {
            for (LocalDate date = request.getStartDate(); !date.isAfter(request.getEndDate()); date = date.plusDays(1)) {
                RoomAvailability availability = availabilityRepository.findByRoom_RoomIdAndDate(roomId, date);
                if (availability != null) {
                    availability.setAvailableRooms(1); // or whatever default value
                    availabilityRepository.save(availability);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to unblock room dates. Please try again later.", e);
        }
    }

    public double getHotelAvailabilityRatio(UUID hotelId, LocalDate checkIn, LocalDate checkOut) {
        try {
            List<Room> rooms = roomRepository.findAllByHotel_HotelId(hotelId);
            int totalRooms = rooms.stream().mapToInt(Room::getTotalRooms).sum();
            if (totalRooms == 0) return 0.0;

            int days = 0;
            double totalRatio = 0.0;

            for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
                int availableRooms = 0;
                for (Room room : rooms) {
                    RoomAvailability availability = availabilityRepository.findByRoom_RoomIdAndDate(room.getRoomId(), date);
                    if (availability != null) {
                        availableRooms += availability.getAvailableRooms();
                    } else {
                        availableRooms += room.getTotalRooms(); // If no record, assume all available
                    }
                }
                totalRatio += ((double) availableRooms / totalRooms);
                days++;
            }
            return days == 0 ? 1.0 : totalRatio / days;
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hotel availability ratio. Please try again later.", e);
        }
    }

}
