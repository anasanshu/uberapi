package com.uber.uberapi.controller;

import com.uber.uberapi.exception.InvalidDriverException;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.ExactLocation;
import com.uber.uberapi.models.Passenger;
import com.uber.uberapi.repositories.DriverRepository;
import com.uber.uberapi.repositories.PassengerRepository;
import com.uber.uberapi.services.locationService.LocationTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/location")
public class LocationTrackingController {
    @Autowired
    PassengerRepository passengerRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    LocationTrackingService locationTrackingService;

    public Driver getDriverFromId(Long driverId){
        Optional<Driver> driver = driverRepository.findById(driverId);
        if (driver.isEmpty()) {
            throw new InvalidDriverException("No driver with id - " + driverId);
        }

        return driver.get();
    }

    public Passenger getPassengerFromId(Long passengerId){
        Optional<Passenger> passenger = passengerRepository.findById(passengerId);
        if (passenger.isEmpty()) {
            throw new InvalidDriverException("No driver with id - " + passengerId);
        }

        return passenger.get();
    }

    @PutMapping("/driver/{driverId}")
    public void updateDriverLocation(@PathVariable(name = "driverId") Long driverId,
                                     @RequestBody ExactLocation data) {
        // called once every 3 seconds for each active driver
        Driver driver = getDriverFromId(driverId);
        ExactLocation location = ExactLocation.builder()
                .latitude(data.getLatitude())
                .longitude((data.getLongitude()))
                .build();
        // delegate the task to a queue
//        locationTrackingService.updateDriverLocation(driver, location);
    }

    @PutMapping("/passenger/{passengerId}")
    public void updatePassengerLocation(@PathVariable(name = "passengerId") Long passengerId,
                                        @RequestBody ExactLocation data) {
        // only triggers every 30 seconds if the passenger is active
        Passenger passenger = getPassengerFromId(passengerId);
        passenger.setLastKnownLocation(ExactLocation.builder()
                .longitude(data.getLongitude())
                .latitude(data.getLatitude())
                .build());

        passengerRepository.save(passenger);
    }
}
