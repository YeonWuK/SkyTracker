package com.skytracker.service;

import com.skytracker.common.dto.alerts.FlightAlertEventMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightAlertProducer {

    private final KafkaTemplate<String, FlightAlertEventMessageDto> kafkaTemplate;

    public void sendFlightAlert(FlightAlertEventMessageDto messageDto) {
        kafkaTemplate.send("flight-alert", messageDto);
    }
}
