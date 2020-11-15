package com.uber.uberapi.services;

import com.uber.uberapi.exception.InvalidActionForBookingStateException;
import com.uber.uberapi.models.*;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.repositories.DriverRepository;
import com.uber.uberapi.repositories.PassengerRepository;
import com.uber.uberapi.services.OTP.OTPService;
import com.uber.uberapi.services.driverMatching.DriverMatchingService;
import com.uber.uberapi.services.messagequeue.MessageQueue;
import com.uber.uberapi.services.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BookingService {
    @Autowired
    DriverMatchingService driverMatchingService;
    @Autowired
    SchedulingService schedulingService;
    @Autowired
    OTPService otpService;
    @Autowired
    MessageQueue messageQueue;
    @Autowired
    Constants constants;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    PassengerRepository passengerRepository;
    @Autowired
    NotificationService notificationService;
    @Autowired
    DriverRepository driverRepository;

    public void createBooking(Booking booking) {
        if (booking.getStartTime().after(new Date())) {
            booking.setBookingStatus(BookingStatus.SCHEDULED);
            messageQueue.sendMessage(constants.getSchedulingTopicName(), new SchedulingService.Message(booking));
        } else {
            booking.setBookingStatus(BookingStatus.ASSIGNING_DRIVER);
            otpService.sendRideStartOTP(booking.getRideStartOTP()); // otp is send to the passenger once the driver is assigned
            messageQueue.sendMessage(constants.getDriverMatchingTopicName(),
                    new DriverMatchingService.Message(booking));
        }
        bookingRepository.save(booking);
        passengerRepository.save(booking.getPassenger());
    }

    public void acceptBooking(Driver driver, Booking booking) {
        if (!booking.needsDriver()) {
            return;
        }
        if (!driver.canAcceptBooking(constants.getMaxWaitTimeForPreviousRide())) {
            notificationService.notify(driver.getPhoneNumber(), "Cannot accept booking");
            return;
        }
        booking.setDriver(driver);
        driver.setActiveBooking(booking);
        booking.getNotifiedDrivers().clear();
        driver.getAcceptableBookings().clear();
        notificationService.notify(driver.getPhoneNumber(), "Booking Accepted");
        notificationService.notify(booking.getPassenger().getPhoneNumber(), driver.getName() + "is arriving at pickup location");
        bookingRepository.save(booking);
        driverRepository.save(driver);
    }

    public void cancelByDriver(Driver driver, Booking booking) {
        booking.setDriver(null);
        driver.setActiveBooking(null);
        driver.getAcceptableBookings().remove(booking);
        notificationService.notify(booking.getPassenger().getPhoneNumber(),
                "Reassigning Driver");
        notificationService.notify(driver.getPhoneNumber(), "Booking has been cancelled");
        retryBooking(booking);
    }

    public void cancelByPassenger(Passenger passenger, Booking booking) {
        try {
            booking.cancel();
            bookingRepository.save(booking);
        } catch (InvalidActionForBookingStateException inner) {
            notificationService.notify(passenger.getPhoneNumber(),
                    "Cannot cancel the booking now. If your ride is in progress, ask your driver to end the ride");
            throw inner;
        }
    }

    public void updateRoute(Booking booking, List<ExactLocation> route) {
        if (!booking.canChangeRoute()) {
            throw new InvalidActionForBookingStateException("Ride has already been completed or cancelled");
        }
        booking.setRoute(route);
        bookingRepository.save(booking);
        notificationService.notify(booking.getDriver().getPhoneNumber(), "Route has been updated");
    }

    public void retryBooking(Booking booking) {
        createBooking(booking);
    }
}
