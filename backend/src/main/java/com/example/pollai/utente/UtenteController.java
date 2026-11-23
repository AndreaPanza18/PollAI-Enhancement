package com.example.pollai.utente;

import com.example.pollai.utente.dto.LoginRequest;
import com.example.pollai.utente.dto.LoginResponse;
import com.example.pollai.utente.dto.RegisterRequest;
import com.example.pollai.utente.dto.UtenteResponse;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "Utenti", description = "API per registrazione e gestione utenti")
public class UtenteController {

    private static final Logger log = LoggerFactory.getLogger(UtenteController.class);

    @Autowired
    private UtenteService utenteService;

    /**
     * Autentica un utente verificando le credenziali fornite e restituisce un token JWT.
     * <p>
     * Se il body della richiesta è {@code null}, viene restituito immediatamente un errore 400 Bad Request.
     * In caso contrario, delega a {@link UtenteService#login(LoginRequest)} che restituisce un
     * {@link Optional} di {@link LoginResponse}: se vuoto, le credenziali sono errate e viene
     * restituito 401 Unauthorized.
     *
     * @param loginRequest le credenziali dell'utente (email e password)
     * @return 200 OK con il token JWT se il login riesce, 401 Unauthorized se le credenziali sono errate,
     *         400 Bad Request se il body è null
     */
    @Operation(summary = "Autenticazione utente", description = "Verifica le credenziali e restituisce un token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login effettuato con successo",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenziali non valide", content = @Content),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<LoginResponse> response = utenteService.login(loginRequest);
        if (response.isEmpty()) {
            log.info("Login non riuscito per l'utente richiesto");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Login effettuato");
        return ResponseEntity.ok(response.get());
    }

    /**
     * Registra un nuovo utente e restituisce il token JWT associato.
     * <p>
     * Se il body della richiesta è {@code null}, viene restituito un errore 400 Bad Request.
     * In caso contrario, delega a {@link UtenteService#register(RegisterRequest)} che crea
     * l'utente nel database e genera il token JWT.
     *
     * @param registerRequest i dati per la registrazione (nome, email, password, ecc.)
     * @return 201 Created con il token JWT se la registrazione riesce, 400 Bad Request se il body è null
     */
    @Operation(summary = "Registrazione utente", description = "Crea un utente e restituisce il relativo token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utente registrato",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest registerRequest) {
        if (registerRequest == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(utenteService.register(registerRequest));
    }

    /**
     * Restituisce l'elenco dei dati pubblici di tutti gli utenti registrati.
     * <p>
     * Endpoint pubblico che non richiede autenticazione. Delega a
     * {@link UtenteService#getAllUtentiResponse()} per il recupero dei dati.
     *
     * @return lista di {@link UtenteResponse} con i dati pubblici di ciascun utente
     */
    @Operation(summary = "Elenco utenti", description = "Restituisce i dati pubblici di tutti gli utenti.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Elenco restituito",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UtenteResponse.class))))
    })
    @GetMapping("/getAll")
    public List<UtenteResponse> getUsers() {
        return utenteService.getAllUtentiResponse();
    }

    /**
     * Restituisce il profilo dell'utente autenticato, identificato dal token Bearer.
     * <p>
     * Estrae il token JWT dall'header {@code Authorization} tramite {@link #extractBearerToken(String)}.
     * Se l'header manca o è malformato, restituisce 400 Bad Request.
     * In caso di token non valido o scaduto, cattura le eccezioni {@link JwtException} e
     * {@link IllegalArgumentException} restituendo 401 Unauthorized.
     *
     * @param authorizationHeader l'header Authorization con il token Bearer
     * @return 200 OK con il profilo utente, 401 Unauthorized se il token è invalido,
     *         404 Not Found se l'utente non esiste, 400 Bad Request se l'header è mancante
     */
    @Operation(summary = "Profilo utente", description = "Restituisce il profilo associato al token Bearer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profilo restituito",
                    content = @Content(schema = @Schema(implementation = UtenteResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token non valido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utente non trovato", content = @Content),
            @ApiResponse(responseCode = "400", description = "Header Authorization mancante o malformato", content = @Content)
    })
    @GetMapping("/profilo")
    public ResponseEntity<UtenteResponse> profilo(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    ) {
        String token = extractBearerToken(authorizationHeader);
        if (token == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            return ResponseEntity.ok(utenteService.getProfilo(token));
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Restituisce il profilo dell'utente autenticato solo se il "pollaio" è configurato.
     * <p>
     * Segue la stessa logica di estrazione del token di {@link #profilo(String)}.
     * Delega a {@link UtenteService#getAreaUtente(String)} per verificare la configurazione
     * del pollaio e restituire il profilo. Se il pollaio non è configurato, il servizio
     * restituirà un errore 409 Conflict.
     *
     * @param authorizationHeader l'header Authorization con il token Bearer
     * @return 200 OK con il profilo se il pollaio è configurato, 401 Unauthorized se il token è invalido,
     *         404 Not Found se l'utente non esiste, 409 Conflict se il pollaio non è configurato,
     *         400 Bad Request se l'header è mancante
     */
    @Operation(
            summary = "Area utente",
            description = "Restituisce il profilo dell'utente autenticato se il pollaio è configurato."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Area utente disponibile",
                    content = @Content(schema = @Schema(implementation = UtenteResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token non valido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utente non trovato", content = @Content),
            @ApiResponse(responseCode = "409", description = "Pollaio non configurato", content = @Content),
            @ApiResponse(responseCode = "400", description = "Header Authorization mancante o malformato", content = @Content)
    })
    @GetMapping("/area-utente")
    public ResponseEntity<UtenteResponse> areaUtente(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    ) {
        String token = extractBearerToken(authorizationHeader);
        if (token == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            return ResponseEntity.ok(utenteService.getAreaUtente(token));
        } catch (JwtException | IllegalArgumentException e) {
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
}