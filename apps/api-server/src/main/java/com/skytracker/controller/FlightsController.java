package com.skytracker.controller;

import com.skytracker.common.dto.flightSearch.FlightSearchRequestDto;
import com.skytracker.core.service.AmadeusFlightSearchService;
import com.skytracker.service.AmadeusTokenManger;
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
//    private final SearchLogService searchLogService;

    @PostMapping("/search")
    public ResponseEntity<?> searchFlights(@RequestBody @Valid FlightSearchRequestDto flightSearchRequestDto) {

        String token = amadeusService.getAmadeusAccessToken();
        List<?> results = flightSearchService.searchFlights(token, flightSearchRequestDto);
        return ResponseEntity.ok().body(results);
    }

}

