package com.mechconnect.controller;

import com.mechconnect.dto.ApiResponse;
import com.mechconnect.dto.AuthDto;
import com.mechconnect.entity.Customer;
import com.mechconnect.entity.Mechanic;
import com.mechconnect.entity.Role;
import com.mechconnect.entity.User;
import com.mechconnect.repository.CustomerRepository;
import com.mechconnect.repository.MechanicRepository;
import com.mechconnect.repository.UserRepository;
import com.mechconnect.security.CustomUserDetails;
import com.mechconnect.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private MechanicRepository mechanicRepository;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthDto.LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(ApiResponse.success("Login successful",
                new AuthDto.JwtResponse(jwt, userDetails.getId(), userDetails.getEmail(), role)));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthDto.SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Error: Email is already in use!"));
        }
        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRole(signUpRequest.getRole());
        User savedUser = userRepository.save(user);

        if (signUpRequest.getRole() == Role.ROLE_CUSTOMER) {
            Customer customer = new Customer();
            customer.setUser(savedUser);
            customer.setName(signUpRequest.getName());
            customer.setPhone(signUpRequest.getPhone());
            customer.setAddress(signUpRequest.getAddress());
            customerRepository.save(customer);
        } else if (signUpRequest.getRole() == Role.ROLE_MECHANIC) {
            Mechanic mechanic = new Mechanic();
            mechanic.setUser(savedUser);
            mechanic.setWorkshopName(signUpRequest.getWorkshopName());
            mechanic.setPhone(signUpRequest.getPhone());
            mechanic.setAddress(signUpRequest.getAddress());
            mechanic.setStatus(Mechanic.Status.PENDING);
            mechanicRepository.save(mechanic);
        }
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", null));
    }
}
