package com.skytracker.repository;

import com.skytracker.entity.FlightAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightAlertRepository extends JpaRepository<FlightAlert, Long> {
}
