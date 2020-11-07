package com.uber.uberapi.services;

import com.uber.uberapi.models.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchedulingServiceImpl implements SchedulingService {
    @Autowired
    BookingService bookingService;

    @Override
    public void schedule(Booking booking) {
        // if it is time to activate this booking
        bookingService.createBooking(booking);
    }

    public static void main(String[] args) {
        // kafka consumer
    }
}
