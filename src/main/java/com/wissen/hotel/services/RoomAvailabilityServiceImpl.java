package com.wissen.hotel.services;

import com.wissen.hotel.dtos.UpdateInventoryRequest;
import com.wissen.hotel.dtos.BlockRoomRequest;
import com.wissen.hotel.models.Room;
import com.wissen.hotel.models.RoomAvailability;
import com.wissen.hotel.repositories.RoomAvailabilityRepository;
import com.wissen.hotel.repositories.RoomRepository;
import com.wissen.hotel.services.RoomAvailabilityService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityServiceImpl implements RoomAvailabilityService {

    private final RoomAvailabilityRepository availabilityRepository;
    private final RoomRepository roomRepository;

    @Override
    public boolean isRoomAvailable(UUID roomId, LocalDate date) {
        RoomAvailability availability = availabilityRepository.findByRoom_RoomIdAndDate(roomId, date);
        if (availability == null) {
            return false; // If no availability record exists, assume room is available
        }
        return availability.getAvailableRooms() > 0;
    }

    @Override
    public boolean isRoomAvailableForRange(UUID roomId, LocalDate start, LocalDate end) {
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (!isRoomAvailable(roomId, date)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateInventory(UUID roomId, UpdateInventoryRequest request) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        RoomAvailability availability = availabilityRepository.findByRoom_RoomIdAndDate(roomId, request.getDate());

        if (availability == null) {
            availability = RoomAvailability.builder()
                    .room(room)
                    .date(request.getDate())
                    .availableRooms(request.getRoomsAvailable())
                    .build();
        } else {
            availability.setAvailableRooms(request.getRoomsAvailable());
        }

        availabilityRepository.save(availability);
    }

    @Override
    public void blockRoomDates(UUID roomId, BlockRoomRequest request) {
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
    }

    @Override
    public void unblockRoomDates(UUID roomId, BlockRoomRequest request) {
        for (LocalDate date = request.getStartDate(); !date.isAfter(request.getEndDate()); date = date.plusDays(1)) {
            RoomAvailability availability = availabilityRepository.findByRoom_RoomIdAndDate(roomId, date);
            if (availability != null) {
                availability.setAvailableRooms(1); // or whatever default value
                availabilityRepository.save(availability);
            }
        }
    }
}
