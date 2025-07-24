package com.skytracker.controller;

import com.skytracker.dto.alerts.FlightAlertRequestDto;
import com.skytracker.dto.flightSearch.FlightSearchRequestDto;
import com.skytracker.dto.flightSearch.FlightSearchResponseDto;
import com.skytracker.security.auth.CustomUserDetails;
import com.skytracker.service.AmadeusFlightSearchService;
import com.skytracker.service.AmadeusTokenManger;
import com.skytracker.service.PriceAlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/api/flights")
@RestController
@RequiredArgsConstructor
public class FlightsController {

    private final AmadeusFlightSearchService flightSearchService;
    private final AmadeusTokenManger amadeusService;
    private final PriceAlertService priceAlertService;

    @PostMapping("/search")
    public ResponseEntity<?> searchFlights(@RequestBody @Valid FlightSearchRequestDto flightSearchRequestDto) {
        String token = amadeusService.getAmadeusAccessToken();
        List<FlightSearchResponseDto> results = flightSearchService.searchFlights(token, flightSearchRequestDto);
        return ResponseEntity.ok().body(results);
    }

    @PostMapping("/alerts")
    public ResponseEntity<?> registerAlert(@RequestBody @Valid FlightAlertRequestDto dto,
                                           @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getUserId();
        priceAlertService.register(dto, userId);
        return ResponseEntity.ok(Map.of("message", "알림이 성공적으로 등록되었습니다.", "status", "REGISTERED"));
    }
}

