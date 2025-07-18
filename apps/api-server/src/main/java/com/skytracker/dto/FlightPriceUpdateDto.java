package com.skytracker.dto;

import com.skytracker.dto.enums.TravelClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightPriceUpdateDto {
    private String originLocationCode;        // 출발지 코드
    private String destinationLocationCode;   // 도착지 코드
    private String departureDate;             // 출발 날짜
    private String returnDate;                // 귀국 날짜 (왕복일 경우)
    private TravelClass travelClass;          // 클래스 (이코노미, 비즈니스 등)
    private int adults;                       // 성인 인원 수
    private String currencyCode;              // 통화 단위
    private double totalPrice;                // 총 가격
    private LocalDateTime lastUpdatedAt;     // 가격 정보가 마지막으로 업데이트된 시각
}
