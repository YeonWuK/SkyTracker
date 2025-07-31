package com.skytracker.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteAggregationDto {

    private String routeCode;        // ICN:NRT (출발 공항:도착 공항)
    private String departureDate;    // 출발 날짜 (25-08-15)
    private String returnDate;       // 도착 날짜 (25-08-20)
    private long docCount;           // ES 집계 데이터

}
