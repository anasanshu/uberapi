package com.uber.uberapi.services.driverMatching.filters;

import com.uber.uberapi.models.Booking;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.Gender;
import com.uber.uberapi.services.Constants;

import java.util.List;
import java.util.stream.Collectors;

public class GenderFilter extends DriverFilter {

    public GenderFilter(Constants constants) {
        super(constants);
    }

    public List<Driver> apply(List<Driver> drivers, Booking booking) {
        if (!getConstants().getIsGenderFilterEnabled()) return drivers;

        // male drivers can only drive male passengers
        Gender passengerGender = booking.getPassenger().getGender();
        return drivers.stream().filter(driver -> {
            Gender driverGender = driver.getGender();
            return !driverGender.equals(Gender.MALE) || passengerGender.equals(Gender.MALE);
        }).collect(Collectors.toList());
    }
}
