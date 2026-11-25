package com.example.pollai.salute.dto;

import com.example.pollai.pollaio.Gallina;

public record GallinaResponse(Long id, String razza, int eta, int peso) {
    public static GallinaResponse from(Gallina g){
        return new GallinaResponse(
                g.getId(),
                g.getRazza(),
                g.getEta(),
                g.getPeso()
        );
    }
}


