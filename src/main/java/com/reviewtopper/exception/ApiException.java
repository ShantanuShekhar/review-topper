package com.reviewtopper.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    /** Machine-readable code for API consumers (e.g. BUSINESS_RULE, VALIDATION_ERROR). */
    private final String errorCode;

    public ApiException(HttpStatus status, String message) {
        this(status, message, defaultErrorCode(status));
    }

    public ApiException(HttpStatus status, String message, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode != null ? errorCode : defaultErrorCode(status);
    }

    static String defaultErrorCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "BAD_REQUEST";
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case FORBIDDEN -> "FORBIDDEN";
            case NOT_FOUND -> "NOT_FOUND";
            case CONFLICT -> "CONFLICT";
            case UNPROCESSABLE_ENTITY -> "BUSINESS_RULE";
            case INTERNAL_SERVER_ERROR -> "INTERNAL_ERROR";
            default -> "HTTP_" + status.value();
        };
    }
}
