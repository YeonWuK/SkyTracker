package com.skytracker.dto.alerts;

import com.skytracker.entity.FlightAlert;
import com.skytracker.entity.UserFlightAlert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightAlertResponseDto {

    private Long alertId;
    private String origin;
    private String destination;
    private String departureDate;
    private String returnDate;
    private String airlineCode;
    private String flightNumber;
    private String travelClass;
    private String currency;
    private int targetPrice;
    private int lastCheckedPrice;
    private boolean isActive;

    public static FlightAlertResponseDto from(UserFlightAlert userFlightAlert) {
        FlightAlert alert = userFlightAlert.getFlightAlert();
        return FlightAlertResponseDto.builder()
                .alertId(userFlightAlert.getId())
                .origin(alert.getDepartureAirport())
                .destination(alert.getArrivalAirport())
                .departureDate(alert.getDepartureDate())
                .returnDate(alert.getArrivalDate())
                .airlineCode(alert.getAirlineCode())
                .flightNumber(alert.getFlightNumber())
                .travelClass(alert.getTravelClass())
                .currency(alert.getCurrency())
                .targetPrice(alert.getTargetPrice())
                .lastCheckedPrice(alert.getLastCheckedPrice())
                .isActive(userFlightAlert.isActive())
                .build();
    }
}