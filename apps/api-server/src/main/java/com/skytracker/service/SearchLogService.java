package com.skytracker.service;

import com.skytracker.common.dto.SearchLogDto;
import com.skytracker.common.dto.flightSearch.FlightSearchRequestDto;
import com.skytracker.kafkaproducer.service.FlightSearchLogsProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchLogService {

    private final FlightSearchLogsProducer searchLogsProducer;

    public void publishSearchLog(FlightSearchRequestDto req) {
        try {
            SearchLogDto searchLogDto = SearchLogDto.from(req);
            searchLogsProducer.sendSearchLogs(searchLogDto);// Kafka 발행
            log.info("search logs publish succeed: {} → {}", req.getOriginLocationAirport(), req.getDestinationLocationAirport());
        } catch (Exception e) {
            throw new IllegalStateException("search logs publish failed: " + e.getMessage());
        }
    }
}