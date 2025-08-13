package com.skytracker.elasticsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsAggregationDto {

    private String departureAirportCode;  //출발 항공 (ICN)
    private String arrivalAirport;        //도착 항공 (NRT)
    private String departureDate;         // 25-08-10
    private String arrivalDate;           // 25-08-15 or Null
    private long docCount;                // ES에서 수집된 항공권 수

    public static EsAggregationDto from(ParsedRouteDto parsedRouteDto, long docCount) {
        return EsAggregationDto.builder()
                .departureAirportCode(parsedRouteDto.getDepartureAirportCode())
                .arrivalAirport(parsedRouteDto.getArrivalAirportCode())
                .departureDate(parsedRouteDto.getDepartureDate())
                .arrivalDate(parsedRouteDto.getArrivalDate())
                .docCount(docCount)
                .build();
    }
}
