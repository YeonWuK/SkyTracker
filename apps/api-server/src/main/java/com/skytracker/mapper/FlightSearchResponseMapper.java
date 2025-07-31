package com.skytracker.mapper;

import com.skytracker.common.dto.SearchContext;
import com.skytracker.common.dto.flightSearch.FlightSearchResponseDto;

public class FlightSearchResponseMapper {

    public static FlightSearchResponseDto toDto(
            String carrierCode,
            String airlineName,
            String flightNumber,
            String departureTime,
            String arrivalTime,
            String duration,
            int seats,
            boolean hasCheckedBags,
            boolean isRefundable,
            boolean isChangeable,
            String currency,
            int price,
            SearchContext context
    ) {
        return FlightSearchResponseDto.builder()
                .airlineCode(carrierCode)
                .airlineName(airlineName)
                .flightNumber(flightNumber)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .duration(duration)
                .numberOfBookableSeats(seats)
                .hasCheckedBags(hasCheckedBags)
                .isRefundable(isRefundable)
                .isChangeable(isChangeable)
                .currency(currency)
                .price(price)
                .departureAirport(context.originLocationAirPort())
                .arrivalAirport(context.destinationLocationAirPort())
                .travelClass(context.travelClass())
                .build();
    }
}