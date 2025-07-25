package com.skytracker.scheduled;

import com.skytracker.service.FlightTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlightTrackingScheduler {

    private final FlightTrackingService flightTrackingService;

//    @Scheduled(fixedRate = 10 * 60 * 1000)
//    public void collectHotFlights()
}
