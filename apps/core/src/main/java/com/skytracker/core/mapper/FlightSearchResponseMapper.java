package com.skytracker.core.mapper;

import com.skytracker.common.dto.RouteAggregationDto;
import com.skytracker.common.dto.SearchContext;
import com.skytracker.common.dto.enums.TravelClass;
import com.skytracker.common.dto.enums.TripType;
import com.skytracker.common.dto.flightSearch.FlightSearchRequestDto;
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
            SearchContext context,
            TripType tripType
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
                .tripType(tripType)
                .build();
    }

    public static FlightSearchRequestDto toFlightSearchRequestDto(RouteAggregationDto route) {
        return FlightSearchRequestDto.builder()
                .originLocationAirport(route.getRouteCode().split(":")[0])
                .destinationLocationAirPort(route.getRouteCode().split(":")[1])
                .departureDate(route.getDepartureDate())
                .returnDate(route.getReturnDate())
                .travelClass(TravelClass.ECONOMY)
                .adults(1)
                .currencyCode("KRW")
                .build();
    }
}