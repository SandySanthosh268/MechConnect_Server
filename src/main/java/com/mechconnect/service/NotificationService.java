package com.mechconnect.service;

import com.mechconnect.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendBookingStatusUpdate(Long bookingId, String status) {
        String destination = "/topic/booking/" + bookingId;
        ApiResponse<String> response = ApiResponse.success("Booking status updated", status);
        messagingTemplate.convertAndSend(destination, response);
    }
}
