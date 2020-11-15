package com.uber.uberapi.services;

import com.uber.uberapi.repositories.DBConstantRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Constants {
    private static final Integer TEN_MINUTES = 10 * 60 * 1000;
    private static final String DEFAULT_EXPIRY_MINUTES = (10 * 60 * 1000) + "";
    final DBConstantRepository dbConstantRepository;
    Map<String, String> constants = new HashMap<>();

    public Constants(DBConstantRepository dbConstantRepository) {
        this.dbConstantRepository = dbConstantRepository;
        loadConstantsFromDB();
    }

    @Scheduled(fixedRate = TEN_MINUTES)
    private void loadConstantsFromDB() {
        dbConstantRepository.findAll().forEach(dbConstant -> {
            constants.put(dbConstant.getName(), dbConstant.getValue());
        });
    }

    public Integer getRideStartOTPExpiryMinutes() {
        return Integer.parseInt(constants.getOrDefault("rideStartOTPExpiryMinutes", DEFAULT_EXPIRY_MINUTES));
    }

    public String getSchedulingTopicName() {
        return constants.getOrDefault("schedulingTopicName", "schedulingServiceTopic");
    }

    public String getDriverMatchingTopicName() {
        return constants.getOrDefault("driverMatchingTopicName", "driverMatchingServiceTopic");
    }

    public Integer getMaxWaitTimeForPreviousRide() {
        return Integer.parseInt(constants.getOrDefault("maxWaitTimeForPreviousRide", "600000"));
    }

    public Integer getBookingProcessBeforeTime() {
        return Integer.parseInt(constants.getOrDefault("bookingProcessBeforeTime", "600000"));
    }

    public String getLocationTrackingTopicName() {
        return constants.getOrDefault("locationTrackingTopicName", "locationTrackingTopicName");
    }

    public double getMaxDistanceKmForDriverMatching() {
        return Double.parseDouble(constants.getOrDefault("maxDistanceKmForDriverMatching", "2"));
    }

    public Integer getMaxDriverETAMinutes() {
        return Integer.parseInt(constants.getOrDefault("maxDriverETAMinutes", "15"));
    }

    public boolean getIsETABasedFilterEnabled() {
        return Boolean.parseBoolean(constants.getOrDefault("isETABasedFilterEnabled", "true"));
    }

    public boolean getIsGenderFilterEnabled() {
        return Boolean.parseBoolean(constants.getOrDefault("isGenderFilterEnabled", "true"));
    }

    public double getDefaultETASpeedKmph() {
        return Double.parseDouble(constants.getOrDefault("defaultETASpeedKmph", "30.0"));
    }
}