package com.uber.uberapi.services;

import com.uber.uberapi.models.Booking;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.ExactLocation;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.services.locationService.LocationTrackingService;
import com.uber.uberapi.services.messagequeue.MQMessage;
import com.uber.uberapi.services.messagequeue.MessageQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverMatchingService {
    @Autowired
    MessageQueue messageQueue;
    @Autowired
    Constants constants;
    @Autowired
    LocationTrackingService locationTrackingService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    BookingRepository bookingRepository;

    @Scheduled(fixedRate = 1000)
    public void consumer() {
        MQMessage m = messageQueue.consumeMessage(constants.getDriverMatchingTopicName());
        if (m == null) return;

        Message message = (Message) m;
        findNearbyDrivers(message.getBooking());
    }

    private void findNearbyDrivers(Booking booking) {
        ExactLocation pickup = booking.getPickupLocation();
        List<Driver> drivers = locationTrackingService.getDiversNearLocation(pickup);
        if (drivers.isEmpty()) {
            // todo: add surge fee and send notifications to expected drivers nearby
            notificationService.notify(booking.getPassenger().getPhoneNumber(), "No cabs near you");
            return;
        }
        notificationService.notify(booking.getPassenger().getPhoneNumber(),
                String.format("Contacting %s cabs near you", drivers.size()));
        // todo: Chain of Responsibility pattern
        // filter the driver somehow

        if (drivers.isEmpty()) {
            notificationService.notify(booking.getPassenger().getPhoneNumber(), "No cabs near you");
        }
        drivers.forEach(driver -> {
            notificationService.notify(driver.getPhoneNumber(), "booking near you: " + booking.toString());
            driver.getAcceptableBookings().add(booking);
        });
        bookingRepository.save(booking);
    }

    public static void main(String[] args) {
        // consumer
        // for each request
        // call the appropriate method
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Message implements MQMessage {
        private Booking booking;

        @Override
        public String toString() {
            return String.format("Need to find drivers for %s", booking.toString());
        }
    }
}
