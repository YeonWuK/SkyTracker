package com.skytracker.service;

import com.skytracker.dto.FlightSearchRequestDto;
import com.skytracker.dto.FlightSearchResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightTrackingService {

    private final FlightPriceUpdateProducer flightPriceUpdateProducer;
    private final FlightAlertEventProducer flightAlertEventProducer;
    private final AmadeusFlightSearchService amadeusService;

    /**
     * 가격 수집 및 가격변동 이벤트 발행
     */
//    public void collectAndPublishPrices(String accessToken, FlightSearchRequestDto req) {
//        try {
//            List<FlightSearchResponseDto> responses = amadeusService.searchFlights(accessToken, req);
//
//            for (FlightSearchResponseDto responseDto : responses) {
//                flightPriceUpdateProducer.sendPriceUpdate(responseDto);
//            }
//
//            log.info("항공권 가격 수집 및 Kafka 발행 완료 ({}건)", responses.size());
//
//        } catch (Exception e) {
//            log.error("항공권 가격 수집 및 Kafka 발행 실패", e);
//        }
//    }
}
