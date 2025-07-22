package com.skytracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchResponseDto {

    private String airlineCode;             // 항공사 코드 (예: KE)
    private String airlineName;             // 항공사 이름 (예: KOREAN AIR)

    private String flightNumber;            // 항공편 번호 (예: 907)

    private String departureAirport;        // 출발 공항 (예: ICN)
    private String departureTime;           // 출발 시간 (예: 2025-07-25T10:55)

    private String arrivalAirport;          // 도착 공항 (예: LHR)
    private String arrivalTime;             // 도착 시간 (예: 2025-07-25T17:20)

    private String duration;                // 총 비행 시간 (예: PT14H25M)

    private String travelClass;             // 좌석 등급 (예: ECONOMY)

    private int numberOfBookableSeats;      // 예약 가능 좌석 수 (예: 9)

    private boolean hasCheckedBags;         // 수하물 포함 여부 (true: 수하물 포함)
    private boolean isRefundable;           // 환불 가능 여부 (true: 환불 가능)
    private boolean isChangeable;           // 일정 변경 가능 여부 (true: 변경 가능)

    private String currency;                // 통화 단위 (예: KRW)
    private int price;                      // 총 가격 (예: 1118800)

}