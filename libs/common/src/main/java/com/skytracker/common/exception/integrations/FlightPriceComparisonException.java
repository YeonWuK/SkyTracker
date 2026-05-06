package com.skytracker.common.exception.integrations;

import com.skytracker.common.exception.BusinessException;
import com.skytracker.common.exception.ErrorCode;

public class FlightPriceComparisonException extends BusinessException {

    public FlightPriceComparisonException(String detail) {
        super(ErrorCode.FLIGHT_PRICE_COMPARISON_FAILED, detail);
    }

    public FlightPriceComparisonException(String detail, Throwable cause) {
        super(ErrorCode.FLIGHT_PRICE_COMPARISON_FAILED, detail, cause);
    }
}
