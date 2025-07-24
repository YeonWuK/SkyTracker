package com.skytracker.service;

import com.skytracker.dto.alerts.FlightAlertRequestDto;
import com.skytracker.dto.enums.Role;
import com.skytracker.entity.FlightAlert;
import com.skytracker.entity.User;
import com.skytracker.repository.FlightAlertRepository;
import com.skytracker.repository.UserFlightAlertRepository;
import com.skytracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceAlertServiceTest {

    @InjectMocks
    private PriceAlertService priceAlertService;

    @Mock
    private FlightAlertRepository flightAlertRepository;

    @Mock
    private UserFlightAlertRepository userFlightAlertRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void register_ShouldRegisterNewAlert_WhenNotExits() {

        FlightAlertRequestDto dto = FlightAlertRequestDto.builder()
                .airlineCode("KE")
                .flightNumber("907")
                .departureAirport("ICN")
                .arrivalAirport("LHR")
                .departureDate("2025-07-25")
                .travelClass("ECONOMY")
                .currency("KRW")
                .build();

        Long userId = 1L;
        String uniqueKey = dto.buildUniqueKey();
        User user = User.builder().id(userId).email("test@example.com").provider("GOOGLE").role(Role.USER).build();
        FlightAlert flightAlert = FlightAlert.from(dto);

        when(flightAlertRepository.findByUniqueKey(uniqueKey)).thenReturn(Optional.empty());
        when(flightAlertRepository.save(any(FlightAlert.class))).thenReturn(flightAlert);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userFlightAlertRepository.existsByUserAndFlightAlert(user, flightAlert)).thenReturn(false);

        // when & then
        assertDoesNotThrow(() -> priceAlertService.register(dto, userId));
        verify(flightAlertRepository).save(any());
        verify(userFlightAlertRepository).save(any());


    }

}