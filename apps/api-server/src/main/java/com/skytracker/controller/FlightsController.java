package com.skytracker.controller;

import com.skytracker.dto.FlightSearchRequestDto;
import com.skytracker.dto.FlightSearchResponseDto;
import com.skytracker.service.AmadeusFlightSearchService;
import com.skytracker.service.AmadeusTokenManger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/flights")
@RestController
@RequiredArgsConstructor
public class FlightsController {

    private final AmadeusFlightSearchService flightSearchService;
    private final AmadeusTokenManger amadeusService;

    @PostMapping("/search")
    public ResponseEntity<?> searchFlights(@RequestBody @Valid FlightSearchRequestDto flightSearchRequestDto) {
        String token = amadeusService.getAmadeusAccessToken();
        List<FlightSearchResponseDto> results = flightSearchService.searchFlights(token, flightSearchRequestDto);
        return ResponseEntity.ok(results);
    }


}

