package com.example.pollai.common.security;

import com.example.pollai.utente.TokenJWTService;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Trasforma l'header {@code Authorization: Bearer <jwt>} nell'id dell'utente autenticato.
 * Header mancante o malformato -> 400. Token non valido o scaduto -> 401.
 */
@Component
public class BearerAuthResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenJWTService tokenJWTService;

    public BearerAuthResolver(TokenJWTService tokenJWTService) {
        this.tokenJWTService = tokenJWTService;
    }

    public Long resolveUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw headerNonValido();
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw headerNonValido();
        }
        try {
            return Long.valueOf(tokenJWTService.extractUserId(token));
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token non valido", e);
        }
    }

    private ResponseStatusException headerNonValido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Header Authorization mancante o malformato");
    }
}