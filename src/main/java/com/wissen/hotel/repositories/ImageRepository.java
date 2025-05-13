package com.wissen.hotel.repositories;

import com.wissen.hotel.models.Image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {

}