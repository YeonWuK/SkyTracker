package com.skytracker.dto.alerts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightAlertRequestDto {

    private String airlineCode;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private String departureDate;
    private String travelClass;
    private String currency;
    private int adults;
    private int lastCheckedPrice;
    private int newPrice;

    public String buildUniqueKey() {
        return String.join("-",
                departureAirport,
                arrivalAirport,
                departureDate,
                travelClass,
                airlineCode,
                flightNumber
        );
    }
}
