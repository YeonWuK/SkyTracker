package com.skytracker.service;

import com.skytracker.dto.EventAlertDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlightAlertEventProducer {

    private final KafkaTemplate<String, EventAlertDto> kafkaTemplate;

    public void sendAlertEvent(EventAlertDto eventAlertDto) {
        kafkaTemplate.send("flight-alert-update", eventAlertDto);
    }
}
