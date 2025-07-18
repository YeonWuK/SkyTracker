package com.skytracker.service;

import com.skytracker.dto.FlightPriceUpdateDto;
import com.skytracker.dto.FlightSearchRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlightPriceUpdateProducer {

    private final KafkaTemplate<FlightSearchRequestDto, FlightPriceUpdateDto> kafkaTemplate;

        public void sendPriceUpdate(FlightSearchRequestDto requestDto, FlightPriceUpdateDto updateDto) {
        kafkaTemplate.send("flight-price-update", requestDto, updateDto);
    }
}
