package com.skytracker.mapper;

import com.skytracker.common.dto.alerts.FlightAlertRequestDto;
import com.skytracker.entity.FlightAlert;

public class FlightAlertMapper {


    /**
     * FlightAlertRequestDto → FlightAlert 변환
     */
    public static FlightAlert toEntity(FlightAlertRequestDto dto){
        return FlightAlert.builder()
                .airlineCode(dto.getAirlineCode())
                .flightNumber(dto.getFlightNumber())
                .departureAirport(dto.getDepartureAirport())
                .arrivalAirport(dto.getArrivalAirport())
                .departureDate(dto.getDepartureDate())
                .travelClass(dto.getTravelClass())
                .currency(dto.getCurrency())
                .adults(dto.getAdults())
                .lastCheckedPrice(dto.getLastCheckedPrice())
                .build();
    }

    /**
     * FlightAlert → FlightAlertRequestDto 변환
     */
    public static FlightAlertRequestDto from(FlightAlert flightAlert) {
        return FlightAlertRequestDto.builder()
                .flightId(flightAlert.getId())
                .airlineCode(flightAlert.getAirlineCode())
                .flightNumber(flightAlert.getFlightNumber())
                .departureAirport(flightAlert.getDepartureAirport())
                .arrivalAirport(flightAlert.getArrivalAirport())
                .departureDate(flightAlert.getDepartureDate())
                .travelClass(flightAlert.getTravelClass())
                .currency(flightAlert.getCurrency())
                .adults(flightAlert.getAdults())
                .lastCheckedPrice(flightAlert.getLastCheckedPrice())
                .newPrice(flightAlert.getNewPrice())
                .build();
    }

}
