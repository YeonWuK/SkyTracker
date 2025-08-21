package com.skytracker.common.exception;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(Long id) {
        super(ErrorCode.USER_NOT_FOUND, "id=" + id);
    }

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}

