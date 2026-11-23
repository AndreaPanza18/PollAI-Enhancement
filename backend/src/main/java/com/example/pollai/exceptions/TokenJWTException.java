package com.example.pollai.exceptions;

public class TokenJWTException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TokenJWTException(String message) {
        super(message);
    }

    public TokenJWTException(String message, Throwable cause) {
        super(message, cause);
    }
}
