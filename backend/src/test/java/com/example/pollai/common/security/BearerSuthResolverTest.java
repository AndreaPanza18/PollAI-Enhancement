package com.example.pollai.common.security;

import com.example.pollai.utente.TokenJWTService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BearerAuthResolverTest {

    @Mock
    private TokenJWTService tokenJWTService;

    @InjectMocks
    private BearerAuthResolver resolver;

    private void assertStatus(HttpStatus atteso, Executable azione) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, azione);
        assertEquals(atteso, ex.getStatusCode());
    }

    @Test
    void headerNull_400() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> resolver.resolveUserId(null));
    }

    @Test
    void headerSenzaBearer_400() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> resolver.resolveUserId("InvalidFormat"));
    }

    @Test
    void tokenVuoto_400() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> resolver.resolveUserId("Bearer "));
        assertStatus(HttpStatus.BAD_REQUEST, () -> resolver.resolveUserId("Bearer    "));
    }

    @Test
    void tokenValido_restituisceUserId() {
        when(tokenJWTService.extractUserId("tok")).thenReturn("7");

        assertEquals(7L, resolver.resolveUserId("Bearer tok"));
    }

    @Test
    void tokenNonValidoOScaduto_401() {
        when(tokenJWTService.extractUserId("tok")).thenThrow(new JwtException("scaduto"));

        assertStatus(HttpStatus.UNAUTHORIZED, () -> resolver.resolveUserId("Bearer tok"));
    }

    @Test
    void subjectNonNumerico_401() {
        when(tokenJWTService.extractUserId("tok")).thenReturn("abc");

        assertStatus(HttpStatus.UNAUTHORIZED, () -> resolver.resolveUserId("Bearer tok"));
    }
}