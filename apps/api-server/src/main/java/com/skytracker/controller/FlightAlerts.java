package com.skytracker.controller;

import com.skytracker.common.dto.alerts.FlightAlertRequestDto;
import com.skytracker.common.dto.alerts.FlightAlertResponseDto;
import com.skytracker.service.PriceAlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Slf4j
@RequestMapping("/api/flights/alerts")
@RestController
@RequiredArgsConstructor
public class FlightAlerts {

    private final PriceAlertService priceAlertService;

    @GetMapping
    public ResponseEntity<List<FlightAlertResponseDto>> getFlightAlerts(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(priceAlertService.getUserFlightAlerts(userId));
    }

    @PostMapping
    public ResponseEntity<?> registerAlert(@RequestBody @Valid FlightAlertRequestDto dto, @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        priceAlertService.register(dto, userId);
        return ResponseEntity.ok(Map.of("message","알림이 성공적으로 등록되었습니다.","status","REGISTERED"));
    }

    @PatchMapping("/{alertId}/toggle")
    public ResponseEntity<?> toggleAlert(@PathVariable Long alertId, @AuthenticationPrincipal Jwt jwt) {
        priceAlertService.toggleAlert(Long.valueOf(jwt.getSubject()), alertId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{alertId}")
    public ResponseEntity<?> deleteAlert(@PathVariable Long alertId, @AuthenticationPrincipal Jwt jwt) {
        priceAlertService.deleteUserFlightAlert(Long.valueOf(jwt.getSubject()), alertId);
        return ResponseEntity.ok().build();
    }
}
