package com.skytracker.common.exception;

public class FlightAlertNotFoundException extends BusinessException {

    public FlightAlertNotFoundException(Long alertId) {
        super(ErrorCode.ALERT_NOT_FOUND, "alertId=" + alertId);
    }

    public FlightAlertNotFoundException() {
        super(ErrorCode.ALERT_NOT_FOUND);
    }

}
