package com.skytracker.service;

import com.skytracker.dto.FlightSearchRequestDto;
import com.skytracker.dto.SearchLogDto;
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
            log.info("검색 로그 발행 완료: {} → {}", req.getOriginLocationCode(), req.getDestinationLocationCode());
        } catch (Exception e) {
            log.error("검색 로그 발행 실패", e);
        }
    }
}