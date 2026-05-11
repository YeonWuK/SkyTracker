package com.skytracker.service;

import com.skytracker.common.dto.alerts.FlightAlertResponseDto;
import com.skytracker.common.exception.alert.FlightAlertNotFoundException;
import com.skytracker.entity.FlightAlert;
import com.skytracker.entity.UserFlightAlert;
import com.skytracker.repository.FlightAlertRepository;
import com.skytracker.repository.UserFlightAlertRepository;
import com.skytracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceAlertServiceTest {

    @Mock
    private FlightAlertRepository flightAlertRepository;

    @Mock
    private UserFlightAlertRepository userFlightAlertRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PriceAlertService priceAlertService;

    @Test
    void deleteUserFlightAlertDeletesOnlyUserSubscription() {
        Long userId = 1L;
        Long alertId = 10L;
        UserFlightAlert userFlightAlert = UserFlightAlert.builder()
                .id(alertId)
                .build();

        when(userFlightAlertRepository.findByIdAndUserId(alertId, userId))
                .thenReturn(Optional.of(userFlightAlert));

        priceAlertService.deleteUserFlightAlert(userId, alertId);

        verify(userFlightAlertRepository).delete(userFlightAlert);
        verify(flightAlertRepository, never()).delete(any());
    }

    @Test
    void deleteUserFlightAlertThrowsWhenSubscriptionDoesNotBelongToUser() {
        Long userId = 1L;
        Long alertId = 10L;

        when(userFlightAlertRepository.findByIdAndUserId(alertId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> priceAlertService.deleteUserFlightAlert(userId, alertId))
                .isInstanceOf(FlightAlertNotFoundException.class);

        verify(userFlightAlertRepository, never()).delete(any());
    }

    @Test
    void toggleAlertChangesActiveState() {
        Long userId = 1L;
        Long alertId = 10L;
        UserFlightAlert userFlightAlert = UserFlightAlert.builder()
                .id(alertId)
                .isActive(false)
                .build();

        when(userFlightAlertRepository.findByIdAndUserId(alertId, userId))
                .thenReturn(Optional.of(userFlightAlert));

        priceAlertService.toggleAlert(userId, alertId);

        assertThat(userFlightAlert.isActive()).isTrue();
    }

    @Test
    void getUserFlightAlertsMapsSubscriptionsWithFlightAlert() {
        Long userId = 1L;
        FlightAlert flightAlert = FlightAlert.builder()
                .airlineCode("KE")
                .flightNumber("907")
                .departureAirport("ICN")
                .arrivalAirport("LHR")
                .departureDate("2026-06-01")
                .arrivalDate("2026-06-10")
                .travelClass("ECONOMY")
                .currency("KRW")
                .targetPrice(500000)
                .lastCheckedPrice(450000)
                .roundTrip(true)
                .nonStop(false)
                .build();
        UserFlightAlert userFlightAlert = UserFlightAlert.builder()
                .id(10L)
                .flightAlert(flightAlert)
                .isActive(true)
                .build();

        when(userFlightAlertRepository.findAllByUserId(userId))
                .thenReturn(List.of(userFlightAlert));

        List<FlightAlertResponseDto> result = priceAlertService.getUserFlightAlerts(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAlertId()).isEqualTo(10L);
        assertThat(result.get(0).getOrigin()).isEqualTo("ICN");
        assertThat(result.get(0).getDestination()).isEqualTo("LHR");
        assertThat(result.get(0).getLastCheckedPrice()).isEqualTo(450000);
        assertThat(result.get(0).isActive()).isTrue();
    }
}
