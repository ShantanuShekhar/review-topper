package com.reviewtopper.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ForgotPasswordResponse(String message, String resetToken) {

    public static ForgotPasswordResponse genericMessage(String message) {
        return new ForgotPasswordResponse(message, null);
    }

    public static ForgotPasswordResponse withDevToken(String message, String resetToken) {
        return new ForgotPasswordResponse(message, resetToken);
    }
}
