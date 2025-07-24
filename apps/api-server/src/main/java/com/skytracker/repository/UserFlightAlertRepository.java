package com.skytracker.repository;

import com.skytracker.entity.FlightAlert;
import com.skytracker.entity.User;
import com.skytracker.entity.UserFlightAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFlightAlertRepository extends JpaRepository<UserFlightAlert, Long> {
    boolean existsByUserAndFlightAlert(User user, FlightAlert flightAlert);
}
