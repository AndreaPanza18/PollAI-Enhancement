package com.example.pollai.salute.dto;

import com.example.pollai.utente.dto.UtenteResponse;

import java.util.List;

public record AccessoSaluteResponse(UtenteResponse utente, List<GallinaResponse> galline) {

}
