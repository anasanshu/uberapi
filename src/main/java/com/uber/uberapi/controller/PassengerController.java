package com.uber.uberapi.controller;

import com.uber.uberapi.exception.InvalidBookingException;
import com.uber.uberapi.exception.InvalidPassengerException;
import com.uber.uberapi.models.*;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.repositories.PassengerRepository;
import com.uber.uberapi.repositories.ReviewRepository;
import com.uber.uberapi.services.BookingService;
import com.uber.uberapi.services.DriverMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/passenger")
public class PassengerController {
    @Autowired
    PassengerRepository passengerRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    BookingService bookingService;

    public Passenger getPassengerFromId(Long passengerId) {
        Optional<Passenger> passenger = passengerRepository.findById(passengerId);
        if (passenger.isEmpty()) {
            throw new InvalidPassengerException("No passenger with id = " + passengerId);
        }

        return passenger.get();
    }

    public Booking getPassengerBookingFromId(Long bookingId, Passenger passenger) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new InvalidBookingException("No Booking with id = " + bookingId);
        }

        Booking booking = optionalBooking.get();
        if (!booking.getPassenger().equals(passenger)) {
            throw new InvalidBookingException("Passenger" + passenger.getId() + " has no such booking" + bookingId);
        }

        return booking;
    }

    @GetMapping("/{passengerId}")
    public Passenger getPassengerDetails(@RequestParam(name = "passengerId") Long passengerId) {
        return getPassengerFromId(passengerId);
    }

    @GetMapping("{passengerId}/bookings")
    public List<Booking> getAllBookings(@RequestParam(name = "passengerId") Long passengerId) {
        Passenger passenger = getPassengerFromId(passengerId);
        return passenger.getBookings();
    }

    @GetMapping("{passengerId}/bookings/{bookingId}")
    public Booking getBooking(@RequestParam(name = "passengerId") Long passengerId,
                              @RequestParam(name = "bookingId") Long bookingId) {
        Passenger passenger = getPassengerFromId(passengerId);
        return getPassengerBookingFromId(bookingId, passenger);
    }

    @PostMapping("{passengerId}/bookings/")
    public void requestBooking(@RequestParam(name = "passengerId") Long passengerId,
                               @RequestBody Booking data) {
        Passenger passenger = getPassengerFromId(passengerId);
        Booking booking = Booking.builder()
                .build();
        bookingService.createBooking(booking);
        passengerRepository.save(passenger);
        bookingRepository.save(booking);

    }

    @DeleteMapping("{passengerId}/bookings/{bookingId}")
    public void cancelBooking(@RequestParam(name = "passengerId") Long passengerId,
                              @RequestParam(name = "bookingId") Long bookingId) {
        Passenger passenger = getPassengerFromId(passengerId);
        Booking booking = getPassengerBookingFromId(bookingId, passenger);
        bookingService.cancelByPassenger(passenger, booking);
    }

    @PatchMapping("{passengerId}/bookings/{bookingId}/rate")
    public void rateRide(@RequestParam(name = "passengerId") Long passengerId,
                         @RequestParam(name = "bookingId") Long bookingId,
                         @RequestBody Review data) {
        Passenger passenger = getPassengerFromId(passengerId);
        Booking booking = getPassengerBookingFromId(bookingId, passenger);
        Review review = Review.builder()
                .note(data.getNote())
                .ratingOutOfFive(data.getRatingOutOfFive())
                .build();
        booking.setReviewByPassenger(review);
        reviewRepository.save(review);
        bookingRepository.save(booking);
    }
}