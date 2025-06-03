package com.wissen.hotel.repository;

import com.wissen.hotel.model.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, UUID> {
    @Query("SELECT r FROM PricingRule r WHERE r.hotel.hotelId = :hotelId AND r.startDate <= :end AND r.endDate >= :start")
    List<PricingRule> findByHotelAndDateRange(@Param("hotelId") UUID hotelId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    List<PricingRule> findByHotel_HotelId(UUID hotelId);
}
