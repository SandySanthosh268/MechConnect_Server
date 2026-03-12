package com.mechconnect.controller;

import com.mechconnect.dto.ApiResponse;
import com.mechconnect.entity.*;
import com.mechconnect.repository.*;
import com.mechconnect.security.CustomUserDetails;
import com.mechconnect.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private MechanicRepository mechanicRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> createBooking(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody CreateBookingRequest request) {
        Customer customer = customerRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Mechanic mechanic = mechanicRepository.findById(request.getMechanicId())
                .orElseThrow(() -> new RuntimeException("Mechanic not found"));
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setMechanic(mechanic);
        booking.setVehicle(vehicle);
        booking.setService(service);
        booking.setBookingDate(request.getBookingDate());
        booking.setPickupRequired(request.getPickupRequired());
        booking.setStatus(Booking.Status.REQUESTED);
        return ResponseEntity.ok(ApiResponse.success("Booking created successfully", bookingRepository.save(booking)));
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> getCustomerBookings(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Customer customer = customerRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return ResponseEntity.ok(ApiResponse.success("Bookings fetched", bookingRepository.findByCustomerId(customer.getId())));
    }

    @GetMapping("/mechanic")
    @PreAuthorize("hasRole('ROLE_MECHANIC')")
    public ResponseEntity<?> getMechanicBookings(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Mechanic mechanic = mechanicRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Mechanic not found"));
        return ResponseEntity.ok(ApiResponse.success("Bookings fetched", bookingRepository.findByMechanicId(mechanic.getId())));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setStatus(request.getStatus());
            Booking updated = bookingRepository.save(booking);
            notificationService.sendBookingStatusUpdate(booking.getId(), updated.getStatus().name());
            return ResponseEntity.ok(ApiResponse.success("Status updated", updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    public static class CreateBookingRequest {
        private Long mechanicId;
        private Long vehicleId;
        private Long serviceId;
        private LocalDate bookingDate;
        private Boolean pickupRequired;
        public Long getMechanicId() { return mechanicId; }
        public void setMechanicId(Long mechanicId) { this.mechanicId = mechanicId; }
        public Long getVehicleId() { return vehicleId; }
        public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
        public Long getServiceId() { return serviceId; }
        public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
        public LocalDate getBookingDate() { return bookingDate; }
        public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
        public Boolean getPickupRequired() { return pickupRequired; }
        public void setPickupRequired(Boolean pickupRequired) { this.pickupRequired = pickupRequired; }
    }

    public static class UpdateStatusRequest {
        private Booking.Status status;
        public Booking.Status getStatus() { return status; }
        public void setStatus(Booking.Status status) { this.status = status; }
    }
}
