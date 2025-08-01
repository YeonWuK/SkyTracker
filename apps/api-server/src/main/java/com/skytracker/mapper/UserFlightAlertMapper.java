package com.skytracker.mapper;

import com.skytracker.common.dto.alerts.FlightAlertEventMessageDto;
import com.skytracker.common.dto.alerts.FlightAlertResponseDto;
import com.skytracker.entity.FlightAlert;
import com.skytracker.entity.UserFlightAlert;

public class UserFlightAlertMapper {

    public static FlightAlertResponseDto toDto(UserFlightAlert userFlightAlert) {
        FlightAlert alert = userFlightAlert.getFlightAlert();

        return FlightAlertResponseDto.builder()
                .alertId(userFlightAlert.getId())
                .origin(alert.getDepartureAirport())
                .destination(alert.getArrivalAirport())
                .flightNumber(alert.getFlightNumber())
                .departureDate(alert.getDepartureDate())
                .travelClass(alert.getTravelClass())
                .airlineCode(alert.getAirlineCode())
                .lastCheckedPrice(alert.getLastCheckedPrice())
                .returnDate(alert.getArrivalDate())
                .currency(alert.getCurrency())
                .isActive(userFlightAlert.isActive())
                .build();
    }

    public static FlightAlertEventMessageDto from(UserFlightAlert userFlightAlert) {
        return FlightAlertEventMessageDto.builder()
                .userId(userFlightAlert.getUser().getId())
                .departureAirport(userFlightAlert.getFlightAlert().getDepartureAirport())
                .arrivalAirport(userFlightAlert.getFlightAlert().getArrivalAirport())
                .departureDate(userFlightAlert.getFlightAlert().getDepartureDate())
                .arrivalDate(userFlightAlert.getFlightAlert().getArrivalDate())
                .lastCheckedPrice(userFlightAlert.getFlightAlert().getLastCheckedPrice())
                .newPrice(userFlightAlert.getFlightAlert().getNewPrice())
                .build();
    }
}