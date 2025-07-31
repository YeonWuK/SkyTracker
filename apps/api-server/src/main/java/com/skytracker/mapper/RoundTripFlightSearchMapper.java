package com.skytracker.mapper;

import com.skytracker.common.dto.SearchContext;
import com.skytracker.common.dto.flightSearch.RoundTripFlightSearchResponseDto;

public class RoundTripFlightSearchMapper {

    /**
     * Amadeus 응답 파싱값들을 기반으로 DTO 생성
     */
    public static RoundTripFlightSearchResponseDto toDto(
            String carrierCode,
            String airlineName,
            String flightNumber,
            String outboundDepartureTime,
            String outboundArrivalTime,
            String outboundDuration,
            String returnDepartureTime,
            String returnArrivalTime,
            String returnDuration,
            int numberOfBookableSeats,
            boolean hasCheckedBags,
            boolean isRefundable,
            boolean isChangeable,
            String currency,
            int price,
            SearchContext searchContext
    ) {
        return RoundTripFlightSearchResponseDto.builder()
                .airlineCode(carrierCode)
                .airlineName(airlineName)
                .flightNumber(flightNumber)
                .departureAirport(searchContext.originLocationAirPort())
                .arrivalAirport(searchContext.destinationLocationAirPort())
                .outboundDepartureTime(outboundDepartureTime)
                .outboundArrivalTime(outboundArrivalTime)
                .outboundDuration(outboundDuration)
                .returnDepartureTime(returnDepartureTime)
                .returnArrivalTime(returnArrivalTime)
                .returnDuration(returnDuration)
                .travelClass(searchContext.travelClass())
                .numberOfBookableSeats(numberOfBookableSeats)
                .hasCheckedBags(hasCheckedBags)
                .isRefundable(isRefundable)
                .isChangeable(isChangeable)
                .currency(currency)
                .price(price)
                .build();
    }
}