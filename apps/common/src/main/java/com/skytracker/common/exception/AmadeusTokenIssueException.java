package com.skytracker.common.exception;

public class AmadeusTokenIssueException extends BusinessException{

    public AmadeusTokenIssueException(String detail) {
        super(ErrorCode.TOKEN_ISSUE_FAILURE, detail);
    }
    public AmadeusTokenIssueException(String detail, Throwable cause) {
        super(ErrorCode.TOKEN_ISSUE_FAILURE, detail, cause);
    }

}
