package com.example.pollai.magazzino;

import com.example.pollai.pollaio.Pollaio;
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

import java.util.Optional;

@Controller
public class MagazzinoController {

    public static final String LOGIN_VIEW = "login";
    public static final String REFERER = "Referer";
    public static final String REDIRECT = "redirect:";
    @Autowired
    private MagazzinoService magazzinoService;
    @Autowired
    private UtenteService utenteService;

    private static final Logger log = LoggerFactory.getLogger(UtenteController.class);



    @GetMapping("/accesso-magazzino")
    public String Magazzino(final HttpSession session, final Model model){
        final Utente utente = getUtente(session);
        if(utente == null) return LOGIN_VIEW;

        Magazzino magazzino;
        if(utente.getMagazzino() == null){
            magazzino = magazzinoService.createMagazzino(utente);
            log.info("Creato Magazzino");
        } else{
            //Se non è il primo accesso l'utente avrà una notifica sulle sue scorte
            magazzino = utente.getMagazzino();
        }
        //aggiorna il magazzino
        utente.setMagazzino(magazzino);
        session.setAttribute("user", utente);
        magazzino.setNotifica();

        return "magazzino";
    }

    @PostMapping("/inserisci-farmaco")
    public String inserisciFarmaco(final HttpSession session,final String tipo_f,final int quantita_f, final HttpServletRequest request){
        final Utente utente = getUtente(session);
        if(utente == null) return LOGIN_VIEW;
        // Ottieni il riferimento della pagina precedente
        final String referer = request.getHeader(REFERER);
        if(quantita_f > 0) {
            // Crea e aggiungi il farmaco al magazzino
            final Farmaco farmaco = new Farmaco(tipo_f, quantita_f, utente.getMagazzino());
            final Magazzino magazzinoAggiornato = magazzinoService.addFarmaco(utente.getMagazzino(), farmaco);
            // Aggiorna l'utente con il magazzino aggiornato
            utente.setMagazzino(magazzinoAggiornato);
            session.setAttribute("user", utente);
        }
        // Reindirizza alla pagina precedente o alla home
        return REDIRECT + (referer != null ? referer : "/");
    }

    @PostMapping("/inserisci-cibo")
    public String inserisciCibo(final HttpSession session,final String tipo_c, final int quantita_c, final HttpServletRequest request){
        final Utente utente = getUtente(session);
        if(utente == null) return LOGIN_VIEW;
        // Ottieni il riferimento della pagina precedente
        final String referer = request.getHeader(REFERER);
        // Crea e aggiungi il cibo al magazzino
        if(quantita_c > 0) {
            final Cibo cibo = new Cibo(tipo_c, quantita_c, utente.getMagazzino());
            final Magazzino magazzinoAggiornato = magazzinoService.addCibo(utente.getMagazzino(), cibo);
            // Aggiorna l'utente con il magazzino aggiornato
            utente.setMagazzino(magazzinoAggiornato);
            session.setAttribute("user", utente);
        }
        // Reindirizza alla pagina precedente o alla home
        return REDIRECT + (referer != null ? referer : "/");
    }

    @PostMapping({"/rimuovi-farmaco", "/rimuovi-cibo"})
    public String rimuoviElemento(final HttpSession session,
                                  @RequestParam("id") final Long itemId,
                                  @RequestParam("tipo") final String itemType,
                                  final HttpServletRequest request) {

        // Recupera l'utente dalla sessione
        final Utente utente = getUtente(session);
        if(utente == null) return LOGIN_VIEW;

        // Ottieni il riferimento della pagina precedente
        final String referer = request.getHeader(REFERER);

        // Log per tipo di elemento
        log.info(itemType + " with id " + itemId);

        // Esegui l'operazione di rimozione in base al tipo (farmaco o cibo)
        Magazzino magazzino = null;
        if ("farmaco".equals(itemType)) {
            magazzino = magazzinoService.removeFarmaco(utente.getMagazzino(), itemId);
        } else if ("cibo".equals(itemType)) {
            magazzino = magazzinoService.removeCibo(utente.getMagazzino(), itemId);
        }

        // Aggiorna l'utente con il magazzino aggiornato
        utente.setMagazzino(magazzino);
        session.setAttribute("user", utente);

        // Reindirizza alla pagina precedente o alla home
        return REDIRECT + (referer != null ? referer : "/");
    }

    @PostMapping({"/modifica-farmaco", "/modifica-cibo"})
    public String modificaElemento(@RequestParam("cambio") int cambio,
                                   @RequestParam("azione") final String azione,
                                   @RequestParam("id") final Long itemId,
                                   @RequestParam("tipo") final String itemType,
                                   final HttpSession session, final HttpServletRequest request){
        // Recupera l'utente dalla sessione
        final Utente utente = getUtente(session);
        if(utente == null) return LOGIN_VIEW;

        // Ottieni il riferimento della pagina precedente
        final String referer = request.getHeader(REFERER);

        //aggiungere o rimuovere cambio in base all'input
        cambio = Math.abs(cambio) * (azione.equals("aggiungi") ? 1 : -1);

        // Esegui l'operazione di modifica in base al tipo (farmaco o cibo)
        Magazzino magazzino = null;
        if ("farmaco".equals(itemType)) {
            magazzino = magazzinoService.modificaFarmaco(utente.getMagazzino(), itemId, cambio);
        } else if ("cibo".equals(itemType)) {
            magazzino = magazzinoService.modificaCibo(utente.getMagazzino(), itemId, cambio);
        }

        // Aggiorna l'utente con il magazzino aggiornato
        utente.setMagazzino(magazzino);
        session.setAttribute("user", utente);

        // Reindirizza alla pagina precedente o alla home
        return REDIRECT + (referer != null ? referer : "/");
    }

    private Utente getUtente(final HttpSession session){
        final Utente utente = (Utente) session.getAttribute("user");

        if (utente == null) {
            log.warn("Nessun utente trovato in sessione.");
            return null;
        }

        final Optional<Utente> optionalUtente = utenteService.getUtenteById(utente.getId());

        if (optionalUtente.isPresent()) {
            return optionalUtente.get();
        } else {
            log.warn("Utente con ID {} non trovato nel database.", utente.getId());
            return null;
        }
    }

}
