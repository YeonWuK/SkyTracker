package com.skytracker.common.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TripType {
    ROUND_TRIP("ROUND_TRIP"),
    ONE_WAY("ONE_WAY");

    private final String value;
}