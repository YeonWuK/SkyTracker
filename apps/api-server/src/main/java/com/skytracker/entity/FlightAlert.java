package com.skytracker.entity;

import com.skytracker.common.dto.alerts.FlightAlertRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightAlert extends BaseTimeEntity{

    @Column(name = "user_flightalert_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "airline_code", nullable = false)
    private String airlineCode;

    @Column(name = "flight_number", nullable = false)
    private String flightNumber;

    @Column(name = "departure_airport", nullable = false)
    private String departureAirport;

    @Column(name = "arrival_airport", nullable = false)
    private String arrivalAirport;

    @Column(name = "departure_date", nullable = false)
    private String departureDate;

    @Column(name = "arrival_date", nullable = false)
    private String arrivalDate;

    @Column(name = "travel_class")
    private String travelClass;

    @Column(name = "currency")
    private String currency;

    @Column(name = "target_price")
    private Integer targetPrice;

    @Column(name = "last_checked_price")
    private Integer lastCheckedPrice;

    @Column(name = "new_Price")
    private Integer newPrice;

    @Column(name = "adults", nullable = false)
    private int adults;

    @Column(name = "unique_key", unique = true, nullable = false)
    private String uniqueKey;

    @OneToMany(mappedBy = "flightAlert", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFlightAlert> userFlightAlerts = new ArrayList<>();

    public void updateNewPrice(int newPrice){
        this.lastCheckedPrice = this.newPrice;
        this.newPrice = newPrice;
    }

    public static FlightAlert from(FlightAlertRequestDto dto){
        return FlightAlert.builder()
                .airlineCode(dto.getAirlineCode())
                .flightNumber(dto.getFlightNumber())
                .departureAirport(dto.getDepartureAirport())
                .arrivalAirport(dto.getArrivalAirport())
                .departureDate(dto.getDepartureDate())
                .travelClass(dto.getTravelClass())
                .currency(dto.getCurrency())
                .adults(dto.getAdults())
                .lastCheckedPrice(dto.getLastCheckedPrice())
                .build();
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

}