package com.mechconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pickup_services")
public class PickupService {

    public enum Status { REQUESTED, ACCEPTED, PICKUP_STARTED, VEHICLE_DELIVERED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "pickup_address")
    private String pickupAddress;

    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;

    @Enumerated(EnumType.STRING)
    private Status status = Status.REQUESTED;

    public PickupService() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public LocalDateTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalDateTime pickupTime) { this.pickupTime = pickupTime; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
