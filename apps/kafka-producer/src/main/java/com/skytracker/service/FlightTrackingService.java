package com.skytracker.service;

import com.skytracker.common.dto.flightSearch.FlightSearchRequestDto;
import com.skytracker.common.dto.flightSearch.FlightSearchResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightTrackingService {

    private final FlightPriceUpdateProducer flightPriceUpdateProducer;
    private final AmadeusFlightSearchService amadeusService;

    /**
     * 가격 수집 및 가격변동 이벤트 발행
     */
    public void collectAndPublishPrices(String accessToken, FlightSearchRequestDto req) {
        try {
            List<?> responses = amadeusService.searchFlights(accessToken, req);

            for (Object responseDto : responses) {
                flightPriceUpdateProducer.sendPriceUpdate((FlightSearchResponseDto) responseDto);
            }

            log.info("항공권 가격 수집 및 Kafka 발행 완료 ({}건)", responses.size());

        } catch (Exception e) {
            log.error("항공권 가격 수집 및 Kafka 발행 실패", e);
        }
    }
}