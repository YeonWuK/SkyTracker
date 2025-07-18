package com.skytracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightTrackingService {

    private final FlightPriceUpdateProducer flightPriceUpdateProducer;
    private final FlightAlertEventProducer flightAlertEventProducer;
    private final AmadeusService amadeusService;

    /**
     * 가격 수집 및 가격변동 이벤트 발행
     */
//    public void
}
