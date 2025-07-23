package com.skytracker.repository;

import com.skytracker.entity.UserFlightAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFlightAlertRepository extends JpaRepository<UserFlightAlert, Long> {
}
