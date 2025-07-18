package com.skytracker.controller;

import com.skytracker.dto.FlightSearchRequestDto;
import com.skytracker.service.AmadeusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/flights")
@RestController
@RequiredArgsConstructor
public class FlightsController {

    private final AmadeusService amadeusService;

    @PostMapping("/search")
    public ResponseEntity<?> searchFlights(@RequestBody FlightSearchRequestDto flightSearchRequestDto) {
        String token = amadeusService.getAmadeusAccessToken();
        String result = amadeusService.searchFlights(token, flightSearchRequestDto);
        return ResponseEntity.ok(result);
    }


}
