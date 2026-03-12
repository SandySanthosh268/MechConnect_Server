package com.mechconnect.controller;

import com.mechconnect.dto.ApiResponse;
import com.mechconnect.dto.UserProfileDto;
import com.mechconnect.entity.Customer;
import com.mechconnect.entity.Mechanic;
import com.mechconnect.entity.Role;
import com.mechconnect.entity.User;
import com.mechconnect.repository.CustomerRepository;
import com.mechconnect.repository.MechanicRepository;
import com.mechconnect.repository.UserRepository;
import com.mechconnect.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private MechanicRepository mechanicRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());

        if (user.getRole() == Role.ROLE_CUSTOMER) {
            Optional<Customer> customerOpt = customerRepository.findByUserId(user.getId());
            if (customerOpt.isPresent()) {
                Customer c = customerOpt.get();
                dto.setCustomerId(c.getId());
                dto.setName(c.getName());
                dto.setPhone(c.getPhone());
                dto.setAddress(c.getAddress());
            }
        } else if (user.getRole() == Role.ROLE_MECHANIC) {
            Optional<Mechanic> mechanicOpt = mechanicRepository.findByUserId(user.getId());
            if (mechanicOpt.isPresent()) {
                Mechanic m = mechanicOpt.get();
                dto.setMechanicId(m.getId());
                dto.setWorkshopName(m.getWorkshopName());
                dto.setPhone(m.getPhone());
                dto.setAddress(m.getAddress());
                dto.setStatus(m.getStatus().name());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", dto));
    }
}
