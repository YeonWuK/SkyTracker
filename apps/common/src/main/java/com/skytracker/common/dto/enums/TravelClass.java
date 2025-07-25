package com.skytracker.common.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TravelClass {
    ECONOMY("ECONOMY"),
    PREMIUM_ECONOMY("PREMIUM_ECONOMY"),
    BUSINESS("BUSINESS"),
    FIRST("FIRST");

    private final String value;
}
