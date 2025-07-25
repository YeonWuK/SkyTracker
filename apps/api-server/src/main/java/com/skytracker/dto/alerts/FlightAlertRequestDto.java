package com.skytracker.dto.alerts;

import com.skytracker.dto.enums.TravelClass;
import com.skytracker.dto.flightSearch.FlightSearchRequestDto;
import com.skytracker.entity.FlightAlert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightAlertRequestDto {

    private Long flightId;                 // 알림 엔티티 ID
    private String airlineCode;            // 항공사 코드 (예: KE)
    private String flightNumber;           // 항공편 번호 (예: 907)
    private String departureAirport;       // 출발 공항 코드 (예: ICN)
    private String arrivalAirport;         // 도착 공항 코드 (예: LHR)
    private String departureDate;          // 출발 날짜 (예: 2025-07-25)
    private String travelClass;            // 좌석 등급 (예: ECONOMY)
    private String currency;               // 통화 코드 (예: KRW)
    private int adults;                    // 성인 인원 수
    private int lastCheckedPrice;          // 마지막으로 확인된 가격
    private int newPrice;                  // 최신 가격 (갱신 후 저장)


    public String buildUniqueKey() {
        return String.join("-",
                departureAirport,
                arrivalAirport,
                departureDate,
                travelClass,
                airlineCode,
                flightNumber
        );
    }

    /**
     * FlightAlert → FlightAlertRequestDto 변환
     */
    public static FlightAlertRequestDto from(FlightAlert flightAlert) {
        return FlightAlertRequestDto.builder()
                .flightId(flightAlert.getId())
                .airlineCode(flightAlert.getAirlineCode())
                .flightNumber(flightAlert.getFlightNumber())
                .departureAirport(flightAlert.getDepartureAirport())
                .arrivalAirport(flightAlert.getArrivalAirport())
                .departureDate(flightAlert.getDepartureDate())
                .travelClass(flightAlert.getTravelClass())
                .currency(flightAlert.getCurrency())
                .adults(flightAlert.getAdults())
                .lastCheckedPrice(flightAlert.getLastCheckedPrice())
                .newPrice(flightAlert.getNewPrice())
                .build();
    }

    /**
     * Amadeus API 호출용 FlightSearchRequestDto로 변환
     */
    public FlightSearchRequestDto toSearchRequest() {
        return FlightSearchRequestDto.builder()
                .originLocationCode(this.departureAirport)
                .destinationLocationCode(this.arrivalAirport)
                .departureDate(this.departureDate)
                .currencyCode(this.currency)
                .travelClass(TravelClass.valueOf(this.travelClass))
                .adults(this.adults)
                .nonStop(true)
                .max(1)
                .build();
    }
}
