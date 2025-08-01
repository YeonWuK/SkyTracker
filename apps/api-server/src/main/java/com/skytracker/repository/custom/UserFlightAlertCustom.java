package com.skytracker.repository.custom;

import com.skytracker.entity.FlightAlert;
import com.skytracker.entity.UserFlightAlert;

import java.util.List;

public interface UserFlightAlertCustom {

    List<UserFlightAlert> findAllByFlightAlert(FlightAlert flightAlert);
}
