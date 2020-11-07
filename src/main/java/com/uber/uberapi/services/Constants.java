package com.uber.uberapi.services;

import com.uber.uberapi.models.DBConstant;
import com.uber.uberapi.repositories.DBConstantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    
    public Integer  getRideStartOTPExpiryMinutes() {
        return Integer.parseInt(constants.getOrDefault("rideStartOTPExpiryMinutes", DEFAULT_EXPIRY_MINUTES));
    }
}
