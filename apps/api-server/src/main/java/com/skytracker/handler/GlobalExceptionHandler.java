package com.skytracker.handler;

import com.skytracker.common.exception.BusinessException;
import com.skytracker.common.exception.ErrorCode;
import com.skytracker.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 서비스에서 던진 커스텀 비즈니스 예외를 공통 에러 응답으로 변환한다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> buildErrorResponseException(BusinessException e) {

        ErrorCode errorCode = e.getErrorCode();
        log.warn("Business exception occurred. code={}, detail={}", errorCode.code, e.getDetail());

        return ResponseEntity.status(errorCode.httpStatus).body(new ErrorResponse(errorCode.code, errorCode.message));
    }

    /**
     * @Valid 검증 실패를 400 응답으로 변환한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> buildValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse(ErrorCode.INVALID_REQUEST.message);

        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.httpStatus)
                .body(new ErrorResponse(ErrorCode.INVALID_REQUEST.code, message));
    }

    /**
     * 예상하지 못한 예외를 내부 서버 오류 응답으로 변환한다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> buildUncaughtException(Exception e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Uncaught exception occurred", e);

        return ResponseEntity.status(status).body(new ErrorResponse(ErrorCode.INTERNAL_ERROR.code, ErrorCode.INTERNAL_ERROR.message));
    }
}
