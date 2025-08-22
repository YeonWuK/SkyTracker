package com.skytracker.kafkaproducer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketUpdateProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

        public void sendTicketUpdate(Object responseDto) {
        kafkaTemplate.send("flight-ticket-update", responseDto);
    }
}