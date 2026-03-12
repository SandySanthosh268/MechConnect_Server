package com.mechconnect.controller;

import com.mechconnect.dto.ApiResponse;
import com.mechconnect.entity.Booking;
import com.mechconnect.entity.Payment;
import com.mechconnect.repository.BookingRepository;
import com.mechconnect.repository.PaymentRepository;
import com.mechconnect.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private BookingRepository bookingRepository;

    @PostMapping("/process")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> processDemoPayment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @RequestBody ProcessPaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getCustomer().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Unauthorized"));
        }
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getMethod());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(Payment.Status.SUCCESS);
        Payment savedPayment = paymentRepository.save(payment);
        booking.setStatus(Booking.Status.PAYMENT_COMPLETED);
        bookingRepository.save(booking);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", savedPayment));
    }

    public static class ProcessPaymentRequest {
        private Long bookingId;
        private Double amount;
        private Payment.Method method;
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public Payment.Method getMethod() { return method; }
        public void setMethod(Payment.Method method) { this.method = method; }
    }
}
