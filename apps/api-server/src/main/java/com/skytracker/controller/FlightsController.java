package com.skytracker.controller;

import com.skytracker.common.dto.HotRouteSummaryDto;
import com.skytracker.common.dto.flightSearch.FlightSearchRequestDto;
import com.skytracker.common.dto.flightSearch.FlightSearchResponseDto;
import com.skytracker.core.service.AmadeusFlightSearchService;
import com.skytracker.core.service.FlightSearchCache;
import com.skytracker.service.token.AmadeusTokenManger;
import com.skytracker.service.HotRankingService;
import com.skytracker.service.SearchLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/api/flights")
@RestController
@RequiredArgsConstructor
public class FlightsController {

    private final AmadeusFlightSearchService flightSearchService;
    private final AmadeusTokenManger amadeusService;
    private final HotRankingService rankingService;
    private final SearchLogService searchLogService;
    private final FlightSearchCache flightSearchCache;

    /**
     * 항공권 검색 요청을 처리하고, 캐시 미스 시 Amadeus API를 조회한다.
     */
    @PostMapping("/search")
    public ResponseEntity<List<FlightSearchResponseDto>> searchFlights(@RequestBody @Valid FlightSearchRequestDto dto) {
        searchLogService.publishSearchLog(dto);
        log.info("Successfully published search log");

        String token = amadeusService.getAmadeusAccessToken();
        String uniqueKey = dto.buildUniqueKey();

        log.info("unique key: {}", uniqueKey);

        // Redis 캐시에 유효한 검색 결과가 있으면 API 호출 없이 반환한다.
        if (flightSearchCache.hasKey(uniqueKey)) {
            List<FlightSearchResponseDto> cachedResults = flightSearchCache.cacheSearch(uniqueKey);
            if (cachedResults != null) {
                log.info("Cache HIT: {}", uniqueKey);
                return ResponseEntity.ok(cachedResults);
            }
            log.info("Cache key exists but value is invalid, falling back to API: {}", uniqueKey);
        }

        // 캐시가 없거나 깨진 경우 실시간 항공권 검색을 수행한다.
        List<FlightSearchResponseDto> results = flightSearchService.searchFlights(token, dto);
        return ResponseEntity.ok(results);
    }

    /**
     * Redis에 캐싱된 인기 노선 요약 정보를 조회한다.
     */
    @GetMapping("/hot-routes")
    public ResponseEntity<List<HotRouteSummaryDto>> getHotRouteBestPrice() {
        List<HotRouteSummaryDto> result = rankingService.getHotRouteSummary();
        return ResponseEntity.ok(result);
    }

}