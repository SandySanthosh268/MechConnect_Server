package com.mechconnect.controller;

import com.mechconnect.dto.ApiResponse;
import com.mechconnect.entity.Mechanic;
import com.mechconnect.repository.CustomerRepository;
import com.mechconnect.repository.MechanicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    @Autowired private MechanicRepository mechanicRepository;
    @Autowired private CustomerRepository customerRepository;

    @GetMapping("/mechanics/pending")
    public ResponseEntity<?> getPendingMechanics() {
        return ResponseEntity.ok(ApiResponse.success("Fetched pending mechanics",
                mechanicRepository.findByStatus(Mechanic.Status.PENDING)));
    }

    @PatchMapping("/mechanics/{id}/approve")
    public ResponseEntity<?> approveMechanic(@PathVariable Long id) {
        return mechanicRepository.findById(id).map(mechanic -> {
            mechanic.setStatus(Mechanic.Status.APPROVED);
            mechanicRepository.save(mechanic);
            return ResponseEntity.ok(ApiResponse.success("Mechanic approved successfully", mechanic));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/mechanics/{id}/reject")
    public ResponseEntity<?> rejectMechanic(@PathVariable Long id) {
        return mechanicRepository.findById(id).map(mechanic -> {
            mechanic.setStatus(Mechanic.Status.REJECTED);
            mechanicRepository.save(mechanic);
            return ResponseEntity.ok(ApiResponse.success("Mechanic rejected", mechanic));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers() {
        return ResponseEntity.ok(ApiResponse.success("Fetched all customers", customerRepository.findAll()));
    }

    @GetMapping("/mechanics")
    public ResponseEntity<?> getAllMechanics() {
        return ResponseEntity.ok(ApiResponse.success("Fetched all mechanics", mechanicRepository.findAll()));
    }
}
