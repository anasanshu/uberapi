package com.uber.uberapi.controller;

import com.uber.uberapi.exception.InvalidBookingException;
import com.uber.uberapi.exception.InvalidDriverException;
import com.uber.uberapi.models.Booking;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.OTP;
import com.uber.uberapi.models.Review;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.repositories.DriverRepository;
import com.uber.uberapi.repositories.ReviewRepository;
import com.uber.uberapi.services.BookingService;
import com.uber.uberapi.services.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/driver")
public class DriverController {
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    BookingService bookingService;
    @Autowired
    Constants constants;


    public Driver getDriverFromId(Long driverId) {
        Optional<Driver> driver = driverRepository.findById(driverId);
        if (driver.isEmpty()) {
            throw new InvalidDriverException("No driver with id = " + driverId);
        }

        return driver.get();
    }

    public Booking getDriverBookingFromId(Long bookingId, Driver driver) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new InvalidBookingException("No Boking with id = " + bookingId);
        }

        Booking booking = optionalBooking.get();
        if (!booking.getDriver().equals(driver)) {
            throw new InvalidBookingException("Driver" + driver.getId() + " has no such booking" + bookingId);
        }

        return booking;
    }

    @GetMapping("/{driverId}")
    public Driver getDriverDetails(@PathVariable(name = "driverId") Long driverId) {
        return getDriverFromId(driverId);
    }

    @PatchMapping("/{driverId}")
    public void changeAvailablility(@PathVariable(name = "driverId") Long driverId,
                                    @RequestBody Boolean available) {
        Driver driver = getDriverFromId(driverId);
        driver.setAvailable(available);
        driverRepository.save(driver);
    }

    @GetMapping("{driverId}/bookings")
    public List<Booking> getAllBookings(@PathVariable(name = "driverId") Long driverId) {
        Driver driver = getDriverFromId(driverId);
        return driver.getBookings();
    }

    @GetMapping("{driverId}/bookings/{bookingId}")
    public Booking getBooking(@PathVariable(name = "driverId") Long driverId,
                              @PathVariable(name = "bookingId") Long bookingId) {
        Driver driver = getDriverFromId(driverId);
        return getDriverBookingFromId(bookingId, driver);
    }

    @PostMapping("{driverId}/bookings/{bookingId}")
    public void acceptBooking(@PathVariable(name = "driverId") Long driverId,
                              @PathVariable(name = "bookingId") Long bookingId) {
        Driver driver = getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId, driver);
        bookingService.acceptBooking(driver, booking);
    }

    @DeleteMapping("{driverId}/bookings/{bookingId}")
    public void cancelBooking(@PathVariable(name = "driverId") Long driverId,
                              @PathVariable(name = "bookingId") Long bookingId) {
        Driver driver = getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId, driver);
        bookingService.cancelByDriver(driver, booking);
    }

    @PatchMapping("{driverId}/bookings/{bookingId}/start")
    public void startRide(@PathVariable(name = "driverId") Long driverId,
                          @PathVariable(name = "bookingId") Long bookingId,
                          @RequestBody OTP otp) {
        Driver driver = getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId, driver);
        booking.startRide(otp, constants.getRideStartOTPExpiryMinutes());
        bookingRepository.save(booking);
    }

    @PatchMapping("{driverId}/bookings/{bookingId}/end")
    public void endRide(@PathVariable(name = "driverId") Long driverId,
                        @PathVariable(name = "bookingId") Long bookingId) {
        Driver driver = getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId, driver);
        booking.endRide();
        driverRepository.save(driver);
        bookingRepository.save(booking);
    }

    @PatchMapping("{driverId}/bookings/{bookingId}/rate")
    public void rateRide(@PathVariable(name = "driverId") Long driverId,
                         @PathVariable(name = "bookingId") Long bookingId,
                         @RequestBody Review data) {
        Driver driver = getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId, driver);
        Review review = Review.builder()
                .note(data.getNote())
                .ratingOutOfFive(data.getRatingOutOfFive())
                .build();
        booking.setReviewByDrier(review);
        reviewRepository.save(review);
        bookingRepository.save(booking);
    }
}
