package com.uber.uberapi.services.locationService;

import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.ExactLocation;
import com.uber.uberapi.services.Constants;
import com.uber.uberapi.services.DriverMatchingService;
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
public class LocationTrackingService {
    @Autowired
    MessageQueue messageQueue;
    @Autowired
    Constants constants;

    @Scheduled(fixedRate = 1000)
    public void consumer() {
        MQMessage m = messageQueue.consumeMessage(constants.getDriverMatchingTopicName());
        if (m == null) return;

        Message message = (Message) m;
        updateDriverLocation(message.getDriver(), message.getLocation());
    }


    public List<Driver> getDiversNearLocation(ExactLocation pickup) {
        // todo
        return null;
    }

    public void updateDriverLocation(Driver driver, ExactLocation location) {
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Message implements MQMessage{
        private Driver driver;
        private ExactLocation location;
    }
}
