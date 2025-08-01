package com.skytracker.repository.custom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skytracker.entity.FlightAlert;
import com.skytracker.entity.UserFlightAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.skytracker.entity.QFlightAlert.flightAlert;
import static com.skytracker.entity.QUser.user;
import static com.skytracker.entity.QUserFlightAlert.userFlightAlert;


@Repository
@RequiredArgsConstructor
public class UserFlightAlertRepositoryCustomImpl implements UserFlightAlertCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserFlightAlert> findAllByFlightAlert(FlightAlert alert) {
        return queryFactory
                .selectFrom(userFlightAlert)
                .join(userFlightAlert.user, user).fetchJoin()
                .join(userFlightAlert.flightAlert, flightAlert).fetchJoin()
                .where(userFlightAlert.flightAlert.eq(alert))
                .fetch();
    }
}
