package com.skytracker.common.exception;

public enum ErrorCode {

    USER_NOT_FOUND(404, "U404", "해당 유저를 찾을 수 없습니다."),
    ALERT_NOT_FOUND(404, "A404", "알림을 찾을 수 없습니다."),
    ALERT_ALREADY_REGISTERED(409, "A409", "이미 등록한 알림입니다."),
    INVALID_REQUEST(400, "C400", "잘못된 요청입니다."),
    TOKEN_ISSUE_FAILURE(503, "E002", "액세스 토큰 발급 실패"),
    LOCK_TIMEOUT(503, "E003", "분산 락 획득/유지 시간 초과"),
    INTERNAL_ERROR(500, "S500", "내부 서버 오류"),
    FLIGHT_SEARCH_FAILED(502, "F001", "항공권 검색 실패"),
    FLIGHT_PRICE_COMPARISON_FAILED(502, "F002", "항공권 가격 비교 실패");

    public final int httpStatus;
    public final String code;
    public final String message;

    ErrorCode(int httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

}