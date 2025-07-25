package com.skytracker.controller;

import com.skytracker.dto.alerts.FlightAlertRequestDto;
import com.skytracker.dto.alerts.FlightAlertResponseDto;
import com.skytracker.security.auth.CustomUserDetails;
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
@RequestMapping("/api/flights/alerts")
@RestController
@RequiredArgsConstructor
public class FlightAlerts {

    private final PriceAlertService priceAlertService;

    @GetMapping
    public ResponseEntity<List<FlightAlertResponseDto>> getFlightAlerts(@AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getUserId();
        List<FlightAlertResponseDto> userFlightAlerts = priceAlertService.getUserFlightAlerts(userId);
        return ResponseEntity.ok(userFlightAlerts);
    }

    @PostMapping
    public ResponseEntity<?> registerAlert(@RequestBody @Valid FlightAlertRequestDto dto,
                                           @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getUserId();
        priceAlertService.register(dto, userId);
        return ResponseEntity.ok(Map.of("message", "알림이 성공적으로 등록되었습니다.", "status", "REGISTERED"));
    }

    @PatchMapping("/{alertId}/toggle")
    public ResponseEntity<?> toggleAlert(@PathVariable Long alertId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        priceAlertService.toggleAlert(userDetails.getUserId(), alertId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{alertId}")
    public ResponseEntity<?> deleteAlert(@PathVariable Long alertId, @AuthenticationPrincipal CustomUserDetails userDetails) {
            Long userId = userDetails.getUserId();
            priceAlertService.deleteUserFlightAlert(userId, alertId);
            return ResponseEntity.ok().build();
    }
}
