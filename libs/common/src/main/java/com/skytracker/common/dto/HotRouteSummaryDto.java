package com.skytracker.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotRouteSummaryDto {

    private int rank;               // 1~10
    private String uniqueKey;       // "YVR:ICN:2025-11-02:2025-11-29:1"
    private String departureAirportCode;
    private String arrivalAirportCode;
    private String departureDate;
    private String arrivalDate;  // 편도면 null
    private int adults;
    private long minPrice; // 최저가

}