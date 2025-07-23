package com.skytracker.dto;

public record SearchContext(
        int adults,
        String originLocationCode,
        String destinationLocationCode,
        String currency,
        String departureDate
) {}