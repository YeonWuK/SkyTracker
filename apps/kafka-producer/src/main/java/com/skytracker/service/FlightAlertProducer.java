package com.skytracker.service;

import com.skytracker.common.dto.alerts.FlightAlertEventMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlightAlertProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendFlightAlert(FlightAlertEventMessageDto messageDto) {
        kafkaTemplate.send("flight-alert", messageDto);
    }
}