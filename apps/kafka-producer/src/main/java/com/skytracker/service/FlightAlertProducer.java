package com.skytracker.service;

import com.skytracker.entity.FlightAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightAlertProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendFlightAlert(FlightAlert flightAlert) {
        kafkaTemplate.send("flight-alert", flightAlert);
    }
}
