package com.skytracker.service;

import com.skytracker.dto.alerts.FlightAlertRequestDto;
import com.skytracker.dto.flightSearch.FlightSearchRequestDto;
import com.skytracker.entity.FlightAlert;
import com.skytracker.entity.User;
import com.skytracker.entity.UserFlightAlert;
import com.skytracker.repository.FlightAlertRepository;
import com.skytracker.repository.UserFlightAlertRepository;
import com.skytracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PriceAlertService {

    private final FlightAlertRepository flightAlertRepository;
    private final UserFlightAlertRepository userFlightAlertRepository;
    private final UserRepository userRepository;

    private final AmadeusTokenManger amadeusTokenManger;
    private final AmadeusFlightSearchService amadeusFlightSearchService;

    public void register(FlightAlertRequestDto dto, Long userId){
        String uniqueKey = dto.buildUniqueKey();

        FlightAlert flightAlert = flightAlertRepository.findByUniqueKey(uniqueKey)
                .orElseGet(() -> flightAlertRepository.save(FlightAlert.from(dto)));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 User 를 찾을 수 없습니다."));

        boolean alreadyRegistered = userFlightAlertRepository.existsByUserAndFlightAlert(user, flightAlert);
        if (alreadyRegistered) {
            log.info("중복 여부: {}", alreadyRegistered);
            throw new IllegalStateException("이미 등록한 알림입니다.");
        }

        UserFlightAlert userFlightAlert = UserFlightAlert.builder()
                .user(user)
                .flightAlert(flightAlert)
                .build();

        userFlightAlertRepository.save(userFlightAlert);
        log.info("알림 등록 완료 - user={}, alert={}", user, uniqueKey);
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void checkPrice() {
        String accessToken = amadeusTokenManger.getAmadeusAccessToken();

        flightAlertRepository.findAll().forEach(alert -> {
            FlightSearchRequestDto requestDto = FlightSearchRequestDto.from(alert);
            amadeusFlightSearchService.searchFlights(accessToken, requestDto);
        });


    }
}