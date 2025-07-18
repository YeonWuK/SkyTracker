package com.skytracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlightAlertEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendAlertEvent(String topic, Object payload) {
        kafkaTemplate.send("flight-alert-update", topic, payload);
    }
}
