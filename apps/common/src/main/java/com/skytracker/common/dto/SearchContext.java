package com.skytracker.common.dto;

import com.skytracker.common.dto.enums.TravelClass;

public record SearchContext(
        int adults,
        String originLocationAirPort,
        String destinationLocationAirPort,
        TravelClass travelClass
) {}