
package com.example.pollai.utente;

import com.example.pollai.common.security.BearerAuthResolver;
import com.example.pollai.utente.dto.LoginRequest;
import com.example.pollai.utente.dto.LoginResponse;
import com.example.pollai.utente.dto.RegisterRequest;
import com.example.pollai.utente.dto.UtenteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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

    private final UtenteService utenteService;
    private final BearerAuthResolver auth;

    public UtenteController(
            UtenteService utenteService,
            BearerAuthResolver auth
    ) {
        this.utenteService = utenteService;
        this.auth = auth;
    }

    @Operation(
            summary = "Autenticazione utente",
            description = "Verifica le credenziali e restituisce un token JWT."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login effettuato con successo",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Credenziali non valide"),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        Optional<LoginResponse> response = utenteService.login(loginRequest);

        if (response.isEmpty()) {
            log.info("Login non riuscito per l'utente richiesto");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Login effettuato");
        return ResponseEntity.ok(response.get());
    }

    @Operation(
            summary = "Registrazione utente",
            description = "Crea un utente e restituisce il relativo token JWT."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Utente registrato",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    })
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(utenteService.register(registerRequest));
    }

    @Operation(
            summary = "Elenco utenti",
            description = "Restituisce i dati degli utenti registrati."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Elenco restituito",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = UtenteResponse.class)
                            )
                    )
            )
    })
    @GetMapping("/getAll")
    public List<UtenteResponse> getUsers() {
        return utenteService.getAllUtentiResponse();
    }

    @Operation(
            summary = "Profilo utente",
            description = "Restituisce il profilo associato al token Bearer."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profilo restituito",
                    content = @Content(schema = @Schema(implementation = UtenteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Header Authorization mancante o malformato",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token non valido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Utente non trovato",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/profilo")
    public UtenteResponse profilo(
            @RequestHeader(name = "Authorization", required = false)
            String authorizationHeader
    ) {
        Long userId = auth.resolveUserId(authorizationHeader);
        return utenteService.getProfilo(userId);
    }

    @Operation(
            summary = "Area utente",
            description = "Restituisce il profilo dell'utente autenticato se il pollaio è configurato."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Area utente disponibile",
                    content = @Content(schema = @Schema(implementation = UtenteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Header Authorization mancante o malformato",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token non valido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Utente non trovato",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Pollaio non configurato",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/area-utente")
    public UtenteResponse areaUtente(
            @RequestHeader(name = "Authorization", required = false)
            String authorizationHeader
    ) {
        Long userId = auth.resolveUserId(authorizationHeader);
        return utenteService.getAreaUtente(userId);
    }
}
