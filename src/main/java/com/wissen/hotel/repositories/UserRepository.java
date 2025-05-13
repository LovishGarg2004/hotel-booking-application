package com.wissen.hotel.repositories;

import com.wissen.hotel.models.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    
}