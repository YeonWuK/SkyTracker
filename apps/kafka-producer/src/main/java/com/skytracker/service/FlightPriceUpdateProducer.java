package com.skytracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlightPriceUpdateProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPriceUpdate(String flightId, String priceUpdate) {
        kafkaTemplate.send("flight-price-update", flightId, priceUpdate);
    }
}
