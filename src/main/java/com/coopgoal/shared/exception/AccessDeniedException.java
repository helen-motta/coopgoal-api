package com.coopgoal.shared.exception;

public class AccessDeniedException extends RuntimeException {
    private final String code;

    public AccessDeniedException(String message) {
        super(message);
        this.code = "ACCESS_DENIED";
    }

    public String getCode() { return code; }
}
