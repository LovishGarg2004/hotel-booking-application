package com.wissen.hotel.repositories;

import com.wissen.hotel.models.PricingRule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PricingRuleRepository extends JpaRepository<PricingRule, UUID> {

}