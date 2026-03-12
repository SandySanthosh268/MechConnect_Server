package com.mechconnect.controller;

import com.mechconnect.dto.ApiResponse;
import com.mechconnect.entity.Mechanic;
import com.mechconnect.entity.Service;
import com.mechconnect.repository.MechanicRepository;
import com.mechconnect.repository.ServiceRepository;
import com.mechconnect.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired private ServiceRepository serviceRepository;
    @Autowired private MechanicRepository mechanicRepository;

    @GetMapping("/mechanic/{mechanicId}")
    public ResponseEntity<?> getMechanicServices(@PathVariable Long mechanicId) {
        return ResponseEntity.ok(ApiResponse.success("Fetched services", serviceRepository.findByMechanicId(mechanicId)));
    }

    @GetMapping("/my-services")
    @PreAuthorize("hasRole('ROLE_MECHANIC')")
    public ResponseEntity<?> getMyServices(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Mechanic mechanic = mechanicRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Mechanic not found"));
        return ResponseEntity.ok(ApiResponse.success("Fetched services", serviceRepository.findByMechanicId(mechanic.getId())));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_MECHANIC')")
    public ResponseEntity<?> addService(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody Service service) {
        Mechanic mechanic = mechanicRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Mechanic not found"));
        service.setMechanic(mechanic);
        return ResponseEntity.ok(ApiResponse.success("Service added", serviceRepository.save(service)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_MECHANIC')")
    public ResponseEntity<?> deleteService(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        serviceRepository.findById(id).ifPresent(service -> {
            if (service.getMechanic().getUser().getId().equals(userDetails.getId())) {
                serviceRepository.delete(service);
            }
        });
        return ResponseEntity.ok(ApiResponse.success("Service deleted", null));
    }
}
