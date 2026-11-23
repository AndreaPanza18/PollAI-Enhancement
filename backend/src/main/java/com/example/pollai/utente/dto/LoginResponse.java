package com.example.pollai.utente.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UtenteResponse user,
        boolean pollaioConfigurato
) {
}
