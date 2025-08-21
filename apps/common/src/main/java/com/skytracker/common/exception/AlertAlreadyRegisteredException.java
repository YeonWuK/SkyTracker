package com.skytracker.common.exception;

public class AlertAlreadyRegisteredException extends BusinessException{

    public AlertAlreadyRegisteredException() {
        super(ErrorCode.ALERT_ALREADY_REGISTERED);
    }

}
