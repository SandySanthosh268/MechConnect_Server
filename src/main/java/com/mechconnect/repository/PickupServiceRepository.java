package com.mechconnect.repository;

import com.mechconnect.entity.PickupService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PickupServiceRepository extends JpaRepository<PickupService, Long> {
    Optional<PickupService> findByBookingId(Long bookingId);
}
