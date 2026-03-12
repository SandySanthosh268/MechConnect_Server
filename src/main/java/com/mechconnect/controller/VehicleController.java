package com.mechconnect.controller;

import com.mechconnect.dto.ApiResponse;
import com.mechconnect.entity.Customer;
import com.mechconnect.entity.Vehicle;
import com.mechconnect.repository.CustomerRepository;
import com.mechconnect.repository.VehicleRepository;
import com.mechconnect.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@PreAuthorize("hasRole('ROLE_CUSTOMER')")
public class VehicleController {

    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private CustomerRepository customerRepository;

    @GetMapping
    public ResponseEntity<?> getMyVehicles(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Customer customer = customerRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return ResponseEntity.ok(ApiResponse.success("Fetched vehicles", vehicleRepository.findByCustomerId(customer.getId())));
    }

    @PostMapping
    public ResponseEntity<?> addVehicle(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody Vehicle vehicle) {
        Customer customer = customerRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        vehicle.setCustomer(customer);
        return ResponseEntity.ok(ApiResponse.success("Vehicle added", vehicleRepository.save(vehicle)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        vehicleRepository.findById(id).ifPresent(vehicle -> {
            if (vehicle.getCustomer().getUser().getId().equals(userDetails.getId())) {
                vehicleRepository.delete(vehicle);
            }
        });
        return ResponseEntity.ok(ApiResponse.success("Vehicle deleted", null));
    }
}
