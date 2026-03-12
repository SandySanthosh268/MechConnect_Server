package com.mechconnect.controller;

import com.mechconnect.dto.ApiResponse;
import com.mechconnect.entity.Mechanic;
import com.mechconnect.repository.MechanicRepository;
import com.mechconnect.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mechanics")
public class MechanicController {

    @Autowired private MechanicRepository mechanicRepository;

    @PutMapping("/profile")
    @PreAuthorize("hasRole('ROLE_MECHANIC')")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody UpdateMechanicProfileDto request) {
        Mechanic mechanic = mechanicRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Mechanic not found"));
        if (request.getWorkshopName() != null) mechanic.setWorkshopName(request.getWorkshopName());
        if (request.getPhone() != null) mechanic.setPhone(request.getPhone());
        if (request.getAddress() != null) mechanic.setAddress(request.getAddress());
        if (request.getLocationLat() != null) mechanic.setLocationLat(request.getLocationLat());
        if (request.getLocationLng() != null) mechanic.setLocationLng(request.getLocationLng());
        return ResponseEntity.ok(ApiResponse.success("Profile updated", mechanicRepository.save(mechanic)));
    }

    @GetMapping("/approved")
    public ResponseEntity<?> getApprovedMechanics() {
        return ResponseEntity.ok(ApiResponse.success("Fetched approved mechanics",
                mechanicRepository.findByStatus(Mechanic.Status.APPROVED)));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchMechanics(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(ApiResponse.success("Search results",
                mechanicRepository.findByStatus(Mechanic.Status.APPROVED).stream().filter(m -> {
                    if (query == null || query.isBlank()) return true;
                    return m.getWorkshopName() != null && m.getWorkshopName().toLowerCase().contains(query.toLowerCase());
                }).toList()));
    }

    public static class UpdateMechanicProfileDto {
        private String workshopName;
        private String phone;
        private String address;
        private Double locationLat;
        private Double locationLng;
        public String getWorkshopName() { return workshopName; }
        public void setWorkshopName(String workshopName) { this.workshopName = workshopName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public Double getLocationLat() { return locationLat; }
        public void setLocationLat(Double locationLat) { this.locationLat = locationLat; }
        public Double getLocationLng() { return locationLng; }
        public void setLocationLng(Double locationLng) { this.locationLng = locationLng; }
    }
}
