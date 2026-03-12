package com.mechconnect.controller;

import com.mechconnect.dto.ApiResponse;
import com.mechconnect.entity.Booking;
import com.mechconnect.entity.Feedback;
import com.mechconnect.repository.BookingRepository;
import com.mechconnect.repository.FeedbackRepository;
import com.mechconnect.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired private FeedbackRepository feedbackRepository;
    @Autowired private BookingRepository bookingRepository;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> submitFeedback(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestBody SubmitFeedbackRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getCustomer().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Unauthorized"));
        }
        Feedback feedback = new Feedback();
        feedback.setBooking(booking);
        feedback.setCustomer(booking.getCustomer());
        feedback.setMechanic(booking.getMechanic());
        feedback.setComments(request.getComments());
        return ResponseEntity.ok(ApiResponse.success("Feedback submitted", feedbackRepository.save(feedback)));
    }

    @GetMapping("/mechanic/{mechanicId}")
    public ResponseEntity<?> getMechanicFeedback(@PathVariable Long mechanicId) {
        return ResponseEntity.ok(ApiResponse.success("Fetched feedback", feedbackRepository.findByMechanicId(mechanicId)));
    }

    public static class SubmitFeedbackRequest {
        private Long bookingId;
        private String comments;
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }
}
