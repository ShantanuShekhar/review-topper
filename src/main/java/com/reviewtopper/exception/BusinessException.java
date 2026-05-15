package com.reviewtopper.exception;

import org.springframework.http.HttpStatus;

/** Business rule violation; exposed to clients as HTTP 422 with {@code error=BUSINESS_RULE}. */
public class BusinessException extends ApiException {

    public BusinessException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message, "BUSINESS_RULE");
    }
}
