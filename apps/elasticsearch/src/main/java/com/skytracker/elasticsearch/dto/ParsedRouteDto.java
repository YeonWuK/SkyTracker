package com.skytracker.elasticsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ParsedRouteDto {

    private String departureAirportCode;
    private String arrivalAirportCode;
    private String departureDate;
    private String arrivalDate;

}