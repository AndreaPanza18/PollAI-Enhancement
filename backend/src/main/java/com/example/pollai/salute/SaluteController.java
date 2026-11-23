package com.example.pollai.salute;

import com.example.pollai.pollaio.Gallina;
import com.example.pollai.pollaio.Pollaio;
import com.example.pollai.utente.TokenJWTService;
import com.example.pollai.utente.Utente;
import com.example.pollai.utente.UtenteDAO;
import com.example.pollai.utente.UtenteService;
import com.example.pollai.utente.dto.UtenteResponse;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/salute")
@Tag(name = "Salute", description = "API per la gestione della salute delle galline")
public class SaluteController {

    private static final Logger log = LoggerFactory.getLogger(SaluteController.class);

    @Autowired
    private UtenteService utenteService;

    @Autowired
    private UtenteDAO utenteDAO;

    @Autowired
    private TokenJWTService tokenJWTService;

    /**
     * Accede alla pagina di gestione salute dell'utente autenticato.
     * <p>
     * Estrae il token JWT dall'header {@code Authorization} tramite {@link #extractBearerToken(String)}.
     * Se l'header manca o è malformato, restituisce 400 Bad Request.
     * In caso di token non valido o scaduto, cattura le eccezioni {@link JwtException} e
     * {@link IllegalArgumentException} restituendo 401 Unauthorized.
     * Se l'utente non ha un pollaio configurato (nullo o senza galline), restituisce 409 Conflict.
     *
     * @param authorizationHeader l'header Authorization con il token Bearer
     * @return 200 OK con i dati delle galline se il pollaio è configurato,
     *         401 Unauthorized se il token è invalido,
     *         404 Not Found se l'utente non esiste,
     *         409 Conflict se il pollaio non è configurato,
     *         400 Bad Request se l'header è mancante
     */
    @Operation(summary = "Accesso salute", description = "Accede alla gestione salute verificando che il pollaio sia configurato.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accesso riuscito, pollaio configurato",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Token non valido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utente non trovato", content = @Content),
            @ApiResponse(responseCode = "409", description = "Pollaio non configurato", content = @Content),
            @ApiResponse(responseCode = "400", description = "Header Authorization mancante o malformato", content = @Content)
    })
    @GetMapping("/accesso-salute")
    public ResponseEntity<Map<String, Object>> accessoSalute(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    ) {
        String token = extractBearerToken(authorizationHeader);
        if (token == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Utente utente = getUtenteFromToken(token);
            Pollaio pollaio = utente.getPollaio();

            // Se non ha galline o è nullo, restituisce 409 Conflict
            if (pollaio == null || pollaio.getQuantity() == 0) {
                log.info("Pollaio non configurato per l'utente ID: {}", utente.getId());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Pollaio non configurato");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            List<Gallina> galline = pollaio.getGalline();
            Map<String, Object> response = new HashMap<>();
            response.put("utente", UtenteResponse.from(utente));
            response.put("galline", galline);

            log.info("Accesso salute effettuato per l'utente ID: {}", utente.getId());
            return ResponseEntity.ok(response);
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Token non valido per accesso salute");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Richiede un consiglio sulla salute di una gallina specifica.
     * <p>
     * Estrae il token JWT dall'header {@code Authorization} tramite {@link #extractBearerToken(String)}.
     * Se l'header manca o è malformato, restituisce 400 Bad Request.
     * In caso di token non valido o scaduto, cattura le eccezioni {@link JwtException} e
     * {@link IllegalArgumentException} restituendo 401 Unauthorized.
     * Verifica che la gallina con l'ID fornito esista nel pollaio dell'utente.
     * Valuta lo stato di salute in base al peso della gallina (range 1500-2500g).
     *
     * @param idGallina l'ID della gallina per cui richiedere il consiglio
     * @param authorizationHeader l'header Authorization con il token Bearer
     * @return 200 OK con il messaggio di salute, 401 Unauthorized se il token è invalido,
     *         404 Not Found se la gallina non esiste, 400 Bad Request se l'header è mancante
     */
    @Operation(summary = "Consiglio salute gallina", description = "Richiede un consiglio sulla salute di una gallina specifica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consiglio restituito",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Token non valido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Gallina non trovata", content = @Content),
            @ApiResponse(responseCode = "400", description = "Header Authorization mancante o malformato", content = @Content)
    })
    @PostMapping("/consiglio/{idGallina}")
    public ResponseEntity<Map<String, String>> richiediConsiglio(
            @PathVariable String idGallina,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    ) {
        String token = extractBearerToken(authorizationHeader);
        if (token == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            int id = Integer.parseInt(idGallina);
            log.info("Richiesto consiglio per la gallina con ID: {}", idGallina);

            Utente utente = getUtenteFromToken(token);
            Pollaio pollaio = utente.getPollaio();

            Optional<Gallina> checkGallina = pollaio.getGallinaByid(id);

            if (checkGallina.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Hen with ID " + idGallina + " not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Gallina gallina = checkGallina.get();
            Map<String, String> response = new HashMap<>();
            if (gallina.getPeso() < 1500 || gallina.getPeso() > 2500) {
                response.put("message", "The hen is not in good health.");
            } else {
                response.put("message", "The hen is in good health.");
            }

            return ResponseEntity.ok(response);
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Token non valido per richiesta consiglio");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Estrae il token JWT dall'header Authorization in formato Bearer.
     * <p>
     * Verifica che l'header non sia {@code null}, inizi con {@code "Bearer "},
     * e che il token estratto non sia vuoto. Restituisce il token pulito (trimmato)
     * oppure {@code null} se la validazione fallisce.
     *
     * @param authorizationHeader l'header Authorization ricevuto nella richiesta HTTP
     * @return il token JWT estratto e pulito, oppure {@code null} se l'header è invalido
     */
    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        return token.isEmpty() ? null : token;
    }

    /**
     * Recupera l'utente a partire dall'ID contenuto nel token JWT,
     * caricando anche il pollaio con le galline tramite {@code EntityGraph}.
     * <p>
     * Utilizza {@link TokenJWTService#extractUserId(String)} per estrarre l'ID utente dal token,
     * poi delega a {@link UtenteDAO#findByIdWithPollaio(Long)} per recuperare l'utente
     * con il pollaio inizializzato dal database.
     * Se l'utente non esiste, lancia un'eccezione {@link RuntimeException}.
     *
     * @param token il token JWT dell'utente autenticato
     * @return l'oggetto {@link Utente} con il pollaio inizializzato
     * @throws RuntimeException se l'utente non è trovato nel database
     */
    private Utente getUtenteFromToken(String token) {
        Long userId = Long.valueOf(tokenJWTService.extractUserId(token));
        return utenteDAO.findByIdWithPollaio(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
    }
}
