package com.example.pollai.utente.dto;

import com.example.pollai.utente.Utente;

public record UtenteResponse(
        Long id,
        String nome,
        String cognome,
        String phoneNumber,
        Boolean rasvsl,
        String pIVA,
        String email
) {
    public static UtenteResponse from(Utente utente) {
        return new UtenteResponse(
                utente.getId(),
                utente.getNome(),
                utente.getCognome(),
                utente.getPhone_number(),
                utente.getRasvsl(),
                utente.getPIVA(),
                utente.getEmail()
        );
    }
}
