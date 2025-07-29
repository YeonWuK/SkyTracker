package com.skytracker.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class RouteAggregationDto {
    private String routeCode;
    private String departureDate;
    private String returnDate;
    private long count;


}
