package com.skytracker.entity;

import com.skytracker.common.dto.alerts.FlightAlertResponseDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFlightAlert extends BaseTimeEntity{

    @Column(name = "user_flight_alert_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id")
    private FlightAlert flightAlert;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public static FlightAlertResponseDto from(UserFlightAlert userFlightAlert) {
        FlightAlert alert = userFlightAlert.getFlightAlert();
        return FlightAlertResponseDto.builder()
                .alertId(userFlightAlert.getId())
                .origin(alert.getDepartureAirport())
                .destination(alert.getArrivalAirport())
                .departureDate(alert.getDepartureDate())
                .returnDate(alert.getArrivalDate())
                .airlineCode(alert.getAirlineCode())
                .flightNumber(alert.getFlightNumber())
                .travelClass(alert.getTravelClass())
                .currency(alert.getCurrency())
                .targetPrice(alert.getTargetPrice())
                .lastCheckedPrice(alert.getLastCheckedPrice())
                .isActive(userFlightAlert.isActive())
                .build();
    }

}
