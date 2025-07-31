package com.skytracker.common.dto.flightSearch;

import com.skytracker.common.dto.enums.TravelClass;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequestDto {

    @NotBlank(message = "출발 공항 코드는 필수입니다.")
    private String originLocationAirport;       // 출발지
    @NotBlank(message = "도착 공항 코드는 필수입니다.")
    private String destinationLocationAirPort;  // 도착지
    @NotBlank(message = "출발 날짜는 필수입니다.")
    private String departureDate;            // 출발일
    private String returnDate;               // 귀국일, ✅ 이 값이 있어야 왕복 요청
    private String currencyCode;             // 통화
    private boolean nonStop;                 // 직항 여부
    private TravelClass travelClass;         // ECONOMY, BUSINESS
    private int adults;                      // 성인 인원
    private int max;                         // 최대 응답 개수

}