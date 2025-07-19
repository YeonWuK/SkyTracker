package com.skytracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    @Column(name = "origin_location", nullable = false)
    private String originLocation;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "lastCheckedPrice")
    private Integer lastCheckedPrice;

    @Column(name = "lastNotifiedPrice")
    private Integer lastNotifiedPrice;

    @OneToMany(mappedBy = "flightAlert", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFlightAlert> userFlightAlerts = new ArrayList<>();

}
