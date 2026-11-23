package com.example.pollai.utente.dto;

public record RegisterRequest(
        String nome,
        String cognome,
        String phoneNumber,
        Boolean rasvsl,
        String pIVA,
        String email,
        String password
) {
}
