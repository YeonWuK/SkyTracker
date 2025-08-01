package com.skytracker.service;

import com.skytracker.common.dto.alerts.FlightAlertEventMessageDto;
import com.skytracker.common.dto.alerts.FlightAlertRequestDto;
import com.skytracker.common.dto.alerts.FlightAlertResponseDto;
import com.skytracker.entity.FlightAlert;
import com.skytracker.entity.User;
import com.skytracker.entity.UserFlightAlert;
import com.skytracker.mapper.FlightAlertMapper;
import com.skytracker.mapper.UserFlightAlertMapper;
import com.skytracker.repository.FlightAlertRepository;
import com.skytracker.repository.UserFlightAlertRepository;
import com.skytracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


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
                .orElseGet(() -> flightAlertRepository.save(FlightAlertMapper.toEntity(dto)));

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
                .isActive(true)
                .build();

        userFlightAlertRepository.save(userFlightAlert);
        log.info("알림 등록 완료 - user={}, alert={}", user, uniqueKey);
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void checkPrice() {
        String accessToken = amadeusTokenManger.getAmadeusAccessToken();

        flightAlertRepository.findAll().forEach(alert -> {
            FlightAlertRequestDto requestDto = FlightAlertMapper.from(alert);
            int lastCheckedPrice = requestDto.getLastCheckedPrice();
            int newPrice = amadeusFlightSearchService.compareFlightsPrice(accessToken, requestDto);

            alert.updateNewPrice(newPrice);
            flightAlertRepository.save(alert);

            // 가격 변동 시 알림 메세지 발행
            if (newPrice < lastCheckedPrice) {
                List<UserFlightAlert> subscribers = userFlightAlertRepository.findAllByFlightAlert(alert);

                if (subscribers.isEmpty()) {
                    throw new IllegalArgumentException("No subscribers found for this flight alert.");
                }
                subscribers.stream()
                        .filter(UserFlightAlert::isActive)
                        .forEach(userFlightAlert -> {
                            FlightAlertEventMessageDto eventMessageDto = UserFlightAlertMapper
                                    .toEventMessageDto(userFlightAlert);
                        });
            }
        });
    }

    public void toggleAlert(Long userId, Long alertId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserFlightAlert alert = userFlightAlertRepository
                .findByUserAndFlightAlertId(user, alertId)
                        .orElseThrow(() -> new IllegalArgumentException("이미 삭제 됬거나, 해당 알림을 찾을 수 없습니다."));

        alert.setActive(!alert.isActive());
    }

    public List<FlightAlertResponseDto> getUserFlightAlerts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userFlightAlertRepository.findAllByUser(user).stream()
                .map(UserFlightAlertMapper::toDto)
                .collect(Collectors.toList());

    }

    public void deleteUserFlightAlert(Long userId, Long alertId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserFlightAlert alert = userFlightAlertRepository.findByUserAndFlightAlertId(user, alertId)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림을 찾을 수 없습니다."));

        userFlightAlertRepository.delete(alert);
        log.info("성공적으로 삭제되었습니다! {}", alertId);
    }
}