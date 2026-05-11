package com.skytracker.service;

import com.skytracker.common.dto.alerts.FlightAlertEventMessageDto;
import com.skytracker.core.constants.RedisKeys;
import com.skytracker.core.service.AmadeusFlightSearchService;
import com.skytracker.core.service.RedisClient;
import com.skytracker.entity.FlightAlert;
import com.skytracker.entity.UserFlightAlert;
import com.skytracker.kafka.service.FlightAlertProducer;
import com.skytracker.mapper.FlightAlertMapper;
import com.skytracker.mapper.UserFlightAlertMapper;
import com.skytracker.repository.FlightAlertRepository;
import com.skytracker.repository.UserFlightAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightAlertService {

    private final RedisClient redisClient;
    private final FlightAlertRepository flightAlertRepository;
    private final UserFlightAlertRepository userFlightAlertRepository;
    private final AmadeusFlightSearchService amadeusFlightSearchService;
    private final FlightAlertProducer flightAlertProducer;

    /**
     *  가격 변동 시 알림 메세지 발행 (3시간)
     */
    @Transactional
    @Scheduled(cron = "0 0 */3 * * *")
    public void publishFlightAlerts() {
        List<FlightAlertEventMessageDto> alertEvents = checkPrice();
        alertEvents.forEach(this::publishFlightAlertEvent);
    }

    /**
     * 날짜가 지난 항공권 삭제(매일 00시)
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanUpFlightAlerts() {
        String today = String.valueOf(LocalDate.now());
        flightAlertRepository.deleteByDepartureDateBefore(today);
    }

    /**
     * 가격 변동 체크
     */
    private List<FlightAlertEventMessageDto> checkPrice() {
        String accessToken = redisClient.getValue(RedisKeys.AMADEUS_TOKEN);

        List<FlightAlertEventMessageDto> eventList = new ArrayList<>();

        flightAlertRepository.findAll().forEach(alert -> {
            try {
                eventList.addAll(checkSingleAlert(accessToken, alert));
            } catch (Exception e) {
                log.error("항공권 알림 가격 체크 실패. alertId={}", alert.getId(), e);
            }
        });
        return eventList;
    }

    private List<FlightAlertEventMessageDto> checkSingleAlert(String accessToken, FlightAlert alert) {
        List<FlightAlertEventMessageDto> eventList = new ArrayList<>();

        Integer lastCheckedPrice = alert.getLastCheckedPrice();
        log.debug("Before id: {}, lastCheckedPrice: {}", alert.getId(), lastCheckedPrice);

        int newPrice = amadeusFlightSearchService.compareFlightsPrice(accessToken, FlightAlertMapper.from(alert));

        if (lastCheckedPrice == null) {
            log.debug("last price is null, alert = {}", alert.getId());
            alert.updateLastCheckedPrice(newPrice);
            flightAlertRepository.save(alert);
            return eventList;
        }

        if (newPrice < lastCheckedPrice) {

            log.info("항공권 가격 하락 감지. alertId={}, before={}, after={}", alert.getId(), lastCheckedPrice, newPrice);

            alert.updateLastCheckedPrice(newPrice);
            flightAlertRepository.save(alert);

            List<UserFlightAlert> subscribers = userFlightAlertRepository.findAllByFlightAlert(alert);

            if (subscribers.isEmpty()) {
                log.warn("가격 하락 알림 구독자가 없습니다. alertId={}", alert.getId());
                return eventList;
            }

            subscribers.stream()
                    .filter(UserFlightAlert::isActive)
                    .map(UserFlightAlertMapper::from)
                    .forEach(eventList::add);
        }

        return eventList;
    }

    private void publishFlightAlertEvent(FlightAlertEventMessageDto event) {
        try {
            flightAlertProducer.sendFlightAlert(event);
        } catch (Exception e) {
            log.error("Kafka 알림 발행 실패. userId={}", event.getUserId(), e);
        }
    }
}
