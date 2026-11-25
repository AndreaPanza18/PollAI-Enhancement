package com.example.pollai.utente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "Formato email non valido")
        @Size(max = 45, message = "L'email non può superare 45 caratteri")
        String email,

        @NotBlank(message = "La password è obbligatoria")

        String password
) {
}