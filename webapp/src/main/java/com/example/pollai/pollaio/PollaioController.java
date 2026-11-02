package com.example.pollai.pollaio;

import com.example.pollai.PollAiApplication;
import com.example.pollai.magazzino.Magazzino;
import com.example.pollai.magazzino.MagazzinoService;
import com.example.pollai.utente.Utente;
import com.example.pollai.utente.UtenteController;
import com.example.pollai.utente.UtenteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class PollaioController {

    private static final Logger log = LoggerFactory.getLogger(UtenteController.class);
    public static final String LOGIN = "login";

    @Autowired
    private PollaioService pollaioService;
    @Autowired
    private UtenteService utenteService;

    @GetMapping("/accesso-pollaio")
    public String Pollaio(final HttpSession session){
        // Recupera l'utente dalla sessione
        final Utente utente = getUtente(session);
        if(utente == null) return LOGIN;
        Pollaio pollaio;

        //Crea il pollaio se non esiste
        if((pollaio = utente.getPollaio()) == null){
            pollaio = pollaioService.createPollaio(utente);
            log.info("Creato pollaio");
        }

        //aggiorna il pollaio
        utente.setPollaio(pollaio);
        session.setAttribute("user", utente);

        return "pollaio";
    }

    @PostMapping("/inserisci-gallina")
    public String inserisciGallina( final HttpSession session,final String razza, final int eta, final int peso, final HttpServletRequest request){
        final Utente utente = getUtente(session);
        if(utente == null) return LOGIN;

        // Ottieni il riferimento della pagina precedente
        final String referer = request.getHeader("Referer");

        final Gallina gallina = new Gallina(razza, eta, peso, utente.getPollaio());
        final Pollaio pollaioAggiornato = pollaioService.addGallina(utente.getPollaio(), gallina);

        utente.setPollaio(pollaioAggiornato);
        session.setAttribute("user", utente);

        // Reindirizza alla pagina precedente o alla home
        return "redirect:" + (referer != null ? referer : "/");
    }

    @PostMapping("/rimuovi-gallina")
    public String rimuoviGallina(@RequestParam("gallinaId") final Long id, final HttpSession session, final HttpServletRequest request) {
        final String referer = request.getHeader("Referer");

        final Utente utente = getUtente(session);
        if (utente == null) return LOGIN;

        final Pollaio pollaio = utente.getPollaio();
        if (pollaio == null) return "pollaio";

        final Optional<Gallina> gallinaDaRimuovere = pollaio.getGallinaByid(id);

        if (gallinaDaRimuovere.isPresent()){
            final Gallina gallina = gallinaDaRimuovere.get();
            final Pollaio pollaioAggiornato = pollaioService.removeGallina(pollaio, gallina);
            utente.setPollaio(pollaioAggiornato);
            session.setAttribute("user", utente);
        }

        return "redirect:" + (referer != null ? referer : "/");
    }


    final Utente getUtente(final HttpSession session){
        //prende l'Utente dalla sessione e verifica se esiste nel database
        final Utente sessionUser = (Utente) session.getAttribute("user");
        if (sessionUser == null) {
            return null;
        }
        final Utente utente = utenteService.getUtenteById(sessionUser.getId()).get();

        return utente;
    }
}
