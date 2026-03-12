package com.mechconnect.controller;

import com.mechconnect.dto.ApiResponse;
import com.mechconnect.entity.Customer;
import com.mechconnect.repository.CustomerRepository;
import com.mechconnect.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasRole('ROLE_CUSTOMER')")
public class CustomerController {

    @Autowired private CustomerRepository customerRepository;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody UpdateProfileDto request) {
        Customer customer = customerRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        if (request.getName() != null) customer.setName(request.getName());
        if (request.getPhone() != null) customer.setPhone(request.getPhone());
        if (request.getAddress() != null) customer.setAddress(request.getAddress());
        return ResponseEntity.ok(ApiResponse.success("Profile updated", customerRepository.save(customer)));
    }

    public static class UpdateProfileDto {
        private String name;
        private String phone;
        private String address;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
}
