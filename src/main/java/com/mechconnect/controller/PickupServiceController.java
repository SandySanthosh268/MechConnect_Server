package com.mechconnect.controller;

import com.mechconnect.dto.ApiResponse;
import com.mechconnect.entity.Booking;
import com.mechconnect.entity.Customer;
import com.mechconnect.entity.Mechanic;
import com.mechconnect.entity.PickupService;
import com.mechconnect.repository.BookingRepository;
import com.mechconnect.repository.CustomerRepository;
import com.mechconnect.repository.MechanicRepository;
import com.mechconnect.repository.PickupServiceRepository;
import com.mechconnect.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pickups")
public class PickupServiceController {

    @Autowired private PickupServiceRepository pickupRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private MechanicRepository mechanicRepository;

    @PostMapping("/request")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> requestPickup(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody CreatePickupRequest request) {
        Customer customer = customerRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Unauthorized to request pickup for this booking"));
        }
        if (pickupRepository.findByBookingId(booking.getId()).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Pickup already requested for this booking"));
        }
        PickupService pickup = new PickupService();
        pickup.setBooking(booking);
        pickup.setPickupAddress(request.getPickupAddress());
        pickup.setPickupTime(request.getPickupTime());
        pickup.setStatus(PickupService.Status.REQUESTED);
        return ResponseEntity.ok(ApiResponse.success("Pickup requested", pickupRepository.save(pickup)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_MECHANIC')")
    public ResponseEntity<?> updatePickupStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        Mechanic mechanic = mechanicRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Mechanic not found"));
        return pickupRepository.findById(id).map(pickup -> {
            if (!pickup.getBooking().getMechanic().getId().equals(mechanic.getId())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Unauthorized"));
            }
            pickup.setStatus(request.getStatus());
            return ResponseEntity.ok(ApiResponse.success("Status updated", pickupRepository.save(pickup)));
        }).orElse(ResponseEntity.notFound().build());
    }

    public static class CreatePickupRequest {
        private Long bookingId;
        private String pickupAddress;
        private java.time.LocalDateTime pickupTime;
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public String getPickupAddress() { return pickupAddress; }
        public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
        public java.time.LocalDateTime getPickupTime() { return pickupTime; }
        public void setPickupTime(java.time.LocalDateTime pickupTime) { this.pickupTime = pickupTime; }
    }

    public static class UpdateStatusRequest {
        private PickupService.Status status;
        public PickupService.Status getStatus() { return status; }
        public void setStatus(PickupService.Status status) { this.status = status; }
    }
}
