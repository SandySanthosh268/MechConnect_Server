package com.mechconnect.controller;

import com.mechconnect.dto.ApiResponse;
import com.mechconnect.entity.Booking;
import com.mechconnect.entity.Rating;
import com.mechconnect.repository.BookingRepository;
import com.mechconnect.repository.RatingRepository;
import com.mechconnect.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired private RatingRepository ratingRepository;
    @Autowired private BookingRepository bookingRepository;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> submitRating(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @RequestBody SubmitRatingRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getCustomer().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Unauthorized"));
        }
        if (ratingRepository.findByBookingId(booking.getId()).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Rating already submitted for this booking"));
        }
        Rating rating = new Rating();
        rating.setBooking(booking);
        rating.setCustomer(booking.getCustomer());
        rating.setMechanic(booking.getMechanic());
        rating.setRatingValue(request.getRatingValue());
        return ResponseEntity.ok(ApiResponse.success("Rating submitted", ratingRepository.save(rating)));
    }

    @GetMapping("/mechanic/{mechanicId}")
    public ResponseEntity<?> getMechanicRatings(@PathVariable Long mechanicId) {
        return ResponseEntity.ok(ApiResponse.success("Fetched ratings", ratingRepository.findByMechanicId(mechanicId)));
    }

    public static class SubmitRatingRequest {
        private Long bookingId;
        private Integer ratingValue;
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public Integer getRatingValue() { return ratingValue; }
        public void setRatingValue(Integer ratingValue) { this.ratingValue = ratingValue; }
    }
}
