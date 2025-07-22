package com.skytracker.service;

import com.skytracker.dto.FlightSearchRequestDto;
import com.skytracker.dto.FlightSearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlightPriceUpdateProducer {

    private final KafkaTemplate<String, FlightSearchResponseDto> kafkaTemplate;

        public void sendPriceUpdate(FlightSearchResponseDto responseDto) {
        kafkaTemplate.send("flight-price-update", responseDto);
    }
}
