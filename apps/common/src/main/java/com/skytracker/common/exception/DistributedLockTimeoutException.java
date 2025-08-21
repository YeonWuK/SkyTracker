package com.skytracker.common.exception;

public class DistributedLockTimeoutException extends BusinessException{

    public DistributedLockTimeoutException(String detail) {
        super(ErrorCode.LOCK_TIMEOUT, detail);
    }

}
