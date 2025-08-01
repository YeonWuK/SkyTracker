package com.skytracker.repository;

import com.skytracker.entity.FlightAlert;
import com.skytracker.entity.User;
import com.skytracker.entity.UserFlightAlert;
import com.skytracker.repository.custom.UserFlightAlertCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFlightAlertRepository extends JpaRepository<UserFlightAlert, Long>, UserFlightAlertCustom {
    boolean existsByUserAndFlightAlert(User user, FlightAlert flightAlert);
    Optional<UserFlightAlert> findByUserAndFlightAlertId(User user, Long flightAlertId);
    List<UserFlightAlert> findAllByUser(User user);
}
