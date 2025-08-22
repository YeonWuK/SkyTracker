package com.skytracker.controller;

import com.skytracker.common.dto.flightSearch.FlightSearchRequestDto;
import com.skytracker.core.service.AmadeusFlightSearchService;
import com.skytracker.service.AmadeusTokenManger;
import com.skytracker.service.SearchLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.skytracker.elasticsearch.service.RouteAggregationService;

import java.util.List;

@Slf4j
@RequestMapping("/api/flights")
@RestController
@RequiredArgsConstructor
public class FlightsController {

    private final AmadeusFlightSearchService flightSearchService;
    private final AmadeusTokenManger amadeusService;
    private final SearchLogService searchLogService;
    private final RouteAggregationService routeAggregationService;

    @PostMapping("/search")
    public ResponseEntity<?> searchFlights(@RequestBody @Valid FlightSearchRequestDto dto) {
        try {
            searchLogService.publishSearchLog(dto);
            routeAggregationService.updateHotRoutes();
            String token = amadeusService.getAmadeusAccessToken();
            List<?> results = flightSearchService.searchFlights(token, dto);
            return ResponseEntity.ok().body(results);
        } catch (Exception e) {
            log.error("Flight search failed", e);
            return ResponseEntity.internalServerError().body("Internal error: " + e.getMessage());
        }
    }
}

