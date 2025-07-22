package com.skytracker.dto;

import com.skytracker.dto.enums.TravelClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequestDto {

    private String originLocationCode;       // 출발지
    private String destinationLocationCode;  // 도착지
    private String departureDate;            // 출발일
    private String returnDate;               // 귀국일
    private String currencyCode;             // 통화
    private boolean nonStop;                 // 직항 여부
    private TravelClass travelClass;         // ECONOMY, BUSINESS
    private int adults;                      // 성인 인원
    private int max;                         // 최대 응답 개수

}