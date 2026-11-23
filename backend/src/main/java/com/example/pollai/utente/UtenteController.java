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

    @Operation(summary = "Elenco utenti", description = "Restituisce i dati pubblici di tutti gli utenti.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Elenco restituito",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UtenteResponse.class))))
    })
    @GetMapping("/getAll")
    public List<UtenteResponse> getUsers() {
        return utenteService.getAllUtentiResponse();
    }

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

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        return token.isEmpty() ? null : token;
    }
}