package com.skytracker.service;

import com.skytracker.common.dto.alerts.FlightAlertEventMessageDto;
import com.skytracker.common.dto.alerts.FlightAlertRequestDto;
import com.skytracker.entity.UserFlightAlert;
import com.skytracker.mapper.FlightAlertMapper;
import com.skytracker.mapper.UserFlightAlertMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightAlertService {





//    private List<FlightAlertEventMessageDto> checkPrice() {
//        String accessToken = amadeusTokenManger.getAmadeusAccessToken();
//
//        List<FlightAlertEventMessageDto> eventList = new ArrayList<>();
//
//        flightAlertRepository.findAll().forEach(alert -> {
//            FlightAlertRequestDto requestDto = FlightAlertMapper.from(alert);
//            int lastCheckedPrice = requestDto.getLastCheckedPrice();
//            int newPrice = amadeusFlightSearchService.compareFlightsPrice(accessToken, requestDto);
//
//            alert.updateNewPrice(newPrice);
//            flightAlertRepository.save(alert);
//
//            // 가격 변동 시 알림 메세지 DTO 생성
//            if (newPrice < lastCheckedPrice) {
//                List<UserFlightAlert> subscribers = userFlightAlertRepository.findAllByFlightAlert(alert);
//
//                if (subscribers.isEmpty()) {
//                    throw new IllegalArgumentException("No subscribers found for this flight alert.");
//                }
//                subscribers.stream()
//                        .filter(UserFlightAlert::isActive)
//                        .map(UserFlightAlertMapper::from)
//                        .forEach(eventList::add);
//            }
//        });
//        return eventList;
//    }
}
