package com.skytracker.common.dto.alerts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class FlightAlertEventMessageDto {
    private Long userId;
    private String departureAirport;
    private String arrivalAirport;
    private String departureDate;
    private String arrivalDate;
    private Integer lastCheckedPrice;
    private Integer newPrice;
}
