package com.skytracker.common.dto;

import com.skytracker.common.dto.flightSearch.FlightSearchRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class SearchLogDto {

    private String originLocationCode;
    private String destinationLocationCode;
    private String departureDate;
    private String returnDate;

    public static SearchLogDto from(FlightSearchRequestDto dto) {
        return SearchLogDto.builder()
                .originLocationCode(dto.getOriginLocationAirport())
                .destinationLocationCode(dto.getDestinationLocationAirPort())
                .departureDate(dto.getDepartureDate())
                .returnDate(dto.getReturnDate())
                .build();
    }
}
