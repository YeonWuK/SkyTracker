package com.skytracker.pricecollector.dto;

import com.skytracker.common.dto.flightSearch.FlightSearchResponseDto;
import com.skytracker.common.dto.flightSearch.RoundTripFlightSearchResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortedRouteDto {

    private String departureAirportCode;
    private String arrivalAirportCode;
    private String departureTime;
    private String arrivalTime;

    public static SortedRouteDto from(FlightSearchResponseDto dto) {
        return SortedRouteDto.builder()
                .departureAirportCode(dto.getDepartureAirport())
                .arrivalAirportCode(dto.getArrivalAirport())
                .departureTime(dto.getDepartureTime().substring(0, 10))
                .arrivalTime(null)
                .build();
    }

    public static SortedRouteDto from(RoundTripFlightSearchResponseDto dto) {
        return SortedRouteDto.builder()
                .departureAirportCode(dto.getDepartureAirport())
                .arrivalAirportCode(dto.getArrivalAirport())
                .departureTime(dto.getOutboundDepartureTime())
                .arrivalTime(dto.getReturnArrivalTime().substring(0, 10))
                .build();
    }
}