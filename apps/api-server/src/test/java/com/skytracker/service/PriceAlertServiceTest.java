package com.skytracker.service;

import com.skytracker.common.dto.alerts.FlightAlertRequestDto;
import com.skytracker.common.dto.alerts.FlightAlertResponseDto;
import com.skytracker.entity.FlightAlert;
import com.skytracker.entity.User;
import com.skytracker.entity.UserFlightAlert;
import com.skytracker.mapper.FlightAlertMapper;
import com.skytracker.repository.FlightAlertRepository;
import com.skytracker.repository.UserFlightAlertRepository;
import com.skytracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PriceAlertServiceTest {

    @Mock private FlightAlertRepository flightAlertRepository;
    @Mock private UserFlightAlertRepository userFlightAlertRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private PriceAlertService priceAlertService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_ShouldSaveUserFlightAlert_WhenNotRegistered() {
        Long userId = 1L;
        FlightAlertRequestDto dto = FlightAlertRequestDto.builder()
                .airlineCode("AA")
                .flightNumber("123")
                .departureAirport("ICN")
                .arrivalAirport("JFK")
                .departureDate("2025-08-01")
                .currency("USD")
                .adults(1)
                .lastCheckedPrice(500)
                .build();

        FlightAlert alert = FlightAlertMapper.toEntity(dto);
        User user = new User();

        when(flightAlertRepository.findByUniqueKey(any())).thenReturn(Optional.empty());
        when(flightAlertRepository.save(any())).thenReturn(alert);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userFlightAlertRepository.existsByUserAndFlightAlert(any(), any())).thenReturn(false);

        assertDoesNotThrow(() -> priceAlertService.register(dto, userId));

        verify(userFlightAlertRepository, times(1)).save(any(UserFlightAlert.class));
    }

    @Test
    void deleteUserFlightAlert_ShouldDeleteEntity() {
        User user = new User();
        FlightAlert alert = new FlightAlert();
        UserFlightAlert ufa = UserFlightAlert.builder().user(user).flightAlert(alert).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userFlightAlertRepository.findByUserAndFlightAlertId(user, 2L)).thenReturn(Optional.of(ufa));

        priceAlertService.deleteUserFlightAlert(1L, 2L);

        verify(userFlightAlertRepository, times(1)).delete(ufa);
    }

    @Test
    void getUserFlightAlerts_ShouldReturnDtos() {
        User user = new User();
        FlightAlert alert = FlightAlert.builder()
                .id(10L).departureAirport("ICN").arrivalAirport("JFK")
                .adults(1).uniqueKey("STR").airlineCode("AA").flightNumber("AB").departureDate("12").arrivalDate("15").lastCheckedPrice(500).build();
        UserFlightAlert ufa = UserFlightAlert.builder().user(user).flightAlert(alert).isActive(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userFlightAlertRepository.findAllByUser(user)).thenReturn(Collections.singletonList(ufa));

        List<FlightAlertResponseDto> result = priceAlertService.getUserFlightAlerts(1L);

        assertEquals(1, result.size());
        assertEquals("ICN", result.get(0).getOrigin());
    }

    @Test
    void register_ShouldThrowException_WhenDuplicateAlertExists() {
        Long userId = 1L;
        FlightAlertRequestDto dto = FlightAlertRequestDto.builder()
                .airlineCode("AA")
                .flightNumber("123")
                .departureAirport("ICN")
                .arrivalAirport("JFK")
                .departureDate("2025-08-01")
                .currency("USD")
                .adults(1)
                .lastCheckedPrice(500)
                .build();

        FlightAlert alert = FlightAlertMapper.toEntity(dto);
        User user = new User();

        when(flightAlertRepository.findByUniqueKey(any())).thenReturn(Optional.of(alert));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userFlightAlertRepository.existsByUserAndFlightAlert(user, alert)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> priceAlertService.register(dto, userId));
    }

    @Test
    void getUserFlightAlerts_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> priceAlertService.getUserFlightAlerts(999L));
    }
}
