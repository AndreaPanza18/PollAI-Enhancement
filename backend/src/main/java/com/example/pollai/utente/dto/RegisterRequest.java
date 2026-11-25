package com.example.pollai.utente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Il nome è obbligatorio")
        @Size(max = 20, message = "Il nome non può superare 20 caratteri")
        String nome,

        @NotBlank(message = "Il cognome è obbligatorio")
        @Size(max = 20, message = "Il cognome non può superare 20 caratteri")
        String cognome,

        @NotBlank(message = "Il numero di telefono è obbligatorio")
        @Size(max = 20, message = "Il numero di telefono non può superare 20 caratteri")
        String phoneNumber,

        Boolean rasvsl,

        @Size(max = 45, message = "La partita IVA non può superare 45 caratteri")
        String pIVA,

        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "Formato email non valido")
        @Size(max = 45, message = "L'email non può superare 45 caratteri")
        String email,

        @NotBlank(message = "La password è obbligatoria")
        @Size(min = 8, max = 72,
                message = "La password deve contenere da 8 a 72 caratteri")
        String password

) {
}