package com.skytracker.common.exception;

public class FlightSearchException extends BusinessException{

    public FlightSearchException(String detail) {
        super(ErrorCode.FLIGHT_SEARCH_FAILED, detail);
    }

    public FlightSearchException(String detail, Throwable cause) {
        super(ErrorCode.FLIGHT_SEARCH_FAILED, detail, cause);
    }

}
