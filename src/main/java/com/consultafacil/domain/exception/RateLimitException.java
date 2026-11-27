package com.consultafacil.domain.exception;

public class RateLimitException extends DomainException {

    private final String code;

    public RateLimitException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
