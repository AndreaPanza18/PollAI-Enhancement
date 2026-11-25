package com.example.pollai.salute;

import com.example.pollai.pollaio.Gallina;
import com.example.pollai.pollaio.Pollaio;
import com.example.pollai.salute.dto.AccessoSaluteResponse;
import com.example.pollai.salute.dto.ConsiglioSaluteResponse;
import com.example.pollai.salute.dto.GallinaResponse;
import com.example.pollai.salute.dto.StatoSalute;
import com.example.pollai.utente.Utente;
import com.example.pollai.utente.UtenteDAO;
import com.example.pollai.utente.dto.UtenteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SaluteService {

    private static final Logger log = LoggerFactory.getLogger(SaluteService.class);

    private static final int PESO_MIN = 1500;
    private static final int PESO_MAX = 2500;
    private static final String POLLAIO_NON_CONFIGURATO = "Pollaio non configurato";

    private final UtenteDAO utenteDAO;

    public SaluteService(UtenteDAO utenteDAO) {
        this.utenteDAO = utenteDAO;
    }

    @Transactional(readOnly = true)
    public AccessoSaluteResponse accessoSalute(Long userId) {
        Utente utente = getUtente(userId);
        Pollaio pollaio = utente.getPollaio();

        if (pollaio == null || pollaio.getQuantity() == 0) {
            log.info("Pollaio non configurato per l'utente ID: {}", userId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, POLLAIO_NON_CONFIGURATO);
        }

        List<GallinaResponse> galline = pollaio.getGalline().stream()
                .map(GallinaResponse::from)
                .toList();

        log.info("Accesso salute effettuato per l'utente ID: {}", userId);
        return new AccessoSaluteResponse(UtenteResponse.from(utente), galline);
    }

    @Transactional(readOnly = true)
    public ConsiglioSaluteResponse richiediConsiglio(Long userId, long idGallina) {
        log.info("Richiesto consiglio per la gallina con ID: {}", idGallina);

        Pollaio pollaio = getUtente(userId).getPollaio();
        if (pollaio == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, POLLAIO_NON_CONFIGURATO);
        }

        Gallina gallina = pollaio.getGallinaByid(idGallina)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Gallina con ID " + idGallina + " non trovata"));

        boolean inSalute = gallina.getPeso() >= PESO_MIN && gallina.getPeso() <= PESO_MAX;

        return inSalute
                ? new ConsiglioSaluteResponse(StatoSalute.BUONA, "The hen is in good health.")
                : new ConsiglioSaluteResponse(StatoSalute.ATTENZIONE, "The hen is not in good health.");
    }

    private Utente getUtente(Long userId) {
        return utenteDAO.findByIdWithPollaio(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));
    }
}