package com.mechconnect.controller;

import com.mechconnect.dto.ApiResponse;
import com.mechconnect.repository.BookingRepository;
import com.mechconnect.repository.CustomerRepository;
import com.mechconnect.repository.MechanicRepository;
import com.mechconnect.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class DashboardController {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private MechanicRepository mechanicRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private VehicleRepository vehicleRepository;

    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalCustomers", customerRepository.count());
        stats.put("totalMechanics", mechanicRepository.count());
        stats.put("totalBookings", bookingRepository.count());
        stats.put("totalVehicles", vehicleRepository.count());
        return ResponseEntity.ok(ApiResponse.success("Fetched admin stats", stats));
    }
}
