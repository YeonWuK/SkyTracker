package com.skytracker.service;

import com.skytracker.common.dto.alerts.FlightAlertEventMessageDto;
import com.skytracker.common.dto.alerts.FlightAlertRequestDto;
import com.skytracker.core.constants.RedisKeys;
import com.skytracker.core.service.AmadeusFlightSearchService;
import com.skytracker.core.service.RedisService;
import com.skytracker.entity.UserFlightAlert;
import com.skytracker.kafkaproducer.service.FlightAlertProducer;
import com.skytracker.mapper.FlightAlertMapper;
import com.skytracker.mapper.UserFlightAlertMapper;
import com.skytracker.repository.FlightAlertRepository;
import com.skytracker.repository.UserFlightAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightAlertService {

    private final RedisService redisService;
    private final FlightAlertRepository flightAlertRepository;
    private final UserFlightAlertRepository userFlightAlertRepository;
    private final AmadeusFlightSearchService amadeusFlightSearchService;
    private final FlightAlertProducer flightAlertProducer;


    /**
     *  가격 변동 시 알림 메세지 발행 (3시간)
     */
    @Scheduled(cron = "0 0 */3 * * *")
    @Transactional
    public void publishFlightAlerts() {
        List<FlightAlertEventMessageDto> alertEvents = checkPrice();
        alertEvents.forEach(flightAlertProducer::sendFlightAlert);
    }


    /**
     * 가격 변동 체크
     */
    private List<FlightAlertEventMessageDto> checkPrice() {
        String accessToken = redisService.getValue(RedisKeys.AMADEUS_TOKEN);

        List<FlightAlertEventMessageDto> eventList = new ArrayList<>();

        flightAlertRepository.findAll().forEach(alert -> {
            FlightAlertRequestDto requestDto = FlightAlertMapper.from(alert);
            int lastCheckedPrice = requestDto.getLastCheckedPrice();
            int newPrice = amadeusFlightSearchService.compareFlightsPrice(accessToken, requestDto);

            alert.updateNewPrice(newPrice);
            flightAlertRepository.save(alert);

            // 가격 변동 시 알림 메세지 DTO 생성
            if (newPrice < lastCheckedPrice) {
                List<UserFlightAlert> subscribers = userFlightAlertRepository.findAllByFlightAlert(alert);

                if (subscribers.isEmpty()) {
                    throw new IllegalArgumentException("해당 항공권의 구독자가 없음");
                }
                subscribers.stream()
                        .filter(UserFlightAlert::isActive)
                        .map(UserFlightAlertMapper::from)
                        .forEach(eventList::add);
            }
        });
        return eventList;
    }
}
