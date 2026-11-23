package com.example.pollai.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenJWTExceptionTest {

    @Test
    void keepsMessage() {
        TokenJWTException exception = new TokenJWTException("Token non valido");

        assertEquals("Token non valido", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void keepsMessageAndCause() {
        IllegalArgumentException cause = new IllegalArgumentException("chiave non valida");

        TokenJWTException exception = new TokenJWTException("Impossibile creare il token", cause);

        assertEquals("Impossibile creare il token", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
