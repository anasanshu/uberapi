package com.uber.uberapi.models;

import com.uber.uberapi.exception.InvalidActionForBookingStateException;
import com.uber.uberapi.exception.InvalidOTPException;
import lombok.*;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "booking", indexes = {
        @Index(columnList = "passenger_id"),
        @Index(columnList = "driver_id")
})
public class Booking extends Auditable {

    @ManyToOne
    private Passenger passenger;

    @ManyToOne
    private Driver driver;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private Set<Driver> notifiedDrivers = new HashSet<>();

    @Enumerated(value = EnumType.STRING)
    private BookingType bookingType;

    @Enumerated(value = EnumType.STRING)
    private BookingStatus bookingStatus;

    @OneToOne
    private Review reviewByPassenger;

    @OneToOne
    private Review reviewByDrier;

    @OneToOne
    private PaymentReceipt paymentReceipt;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "booking_route",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "exact_location_id"),
            indexes = {@Index(columnList = "booking_id")}
    )
    @OrderColumn(name = "location_index")
    private List<ExactLocation> route = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "booking_completed_route",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "exact_location_id"),
            indexes = {@Index(columnList = "booking_id")}
    )
    @OrderColumn(name = "location_index")
    private List<ExactLocation> completedRoute = new ArrayList<>();

    private Long totalDistanceMeters;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date scheduledTime;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date startTime; // actual start time

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date endTime; // actual end time

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date expectedCompletionTime;

    @OneToOne
    private OTP rideStartOTP;

    public void startRide(OTP otp, Integer rideStartOTPExpiryMinutes) {
        if (!bookingStatus.equals(BookingStatus.CAB_ARRIVED)) {
            throw new InvalidActionForBookingStateException("Cannot start ride before driver arrives the pickup point");
        }

        if (!rideStartOTP.validateEnteredOTP(otp, rideStartOTPExpiryMinutes)) {
            throw new InvalidOTPException();
        }
        startTime = new Date();
        bookingStatus = BookingStatus.IN_RIDE;
    }

    public void endRide() {
        if (!bookingStatus.equals(BookingStatus.IN_RIDE)) {
            throw new InvalidActionForBookingStateException("The ride hasn't started yet");
        }

        bookingStatus = BookingStatus.COMPLETED;
        driver.setActiveBooking(null);
    }

    public boolean canChangeRoute() {
        return bookingStatus.equals(BookingStatus.ASSIGNING_DRIVER)
                || bookingStatus.equals(BookingStatus.CAB_ARRIVED)
                || bookingStatus.equals(BookingStatus.REACHING_PICKUP_POINT)
                || bookingStatus.equals(BookingStatus.IN_RIDE)
                || bookingStatus.equals(BookingStatus.SCHEDULED);
    }

    public boolean needsDriver() {
        return bookingStatus.equals(BookingStatus.ASSIGNING_DRIVER);
    }

    public ExactLocation getPickupLocation() {
        return route.get(0);
    }

    public void cancel() {
        if (!(bookingStatus.equals(BookingStatus.REACHING_PICKUP_POINT)
                || bookingStatus.equals(BookingStatus.ASSIGNING_DRIVER)
                || bookingStatus.equals(BookingStatus.SCHEDULED)
                || bookingStatus.equals(BookingStatus.CAB_ARRIVED))) {
            throw new InvalidActionForBookingStateException("Ride cannot be cancelled");
        }
        setBookingStatus(BookingStatus.CANCELLED);
        setDriver(null);
        setNotifiedDrivers(null);
    }
}