package com.example.pollai.salute;

import com.example.pollai.common.security.BearerAuthResolver;
import com.example.pollai.salute.dto.AccessoSaluteResponse;
import com.example.pollai.salute.dto.ConsiglioSaluteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/salute")
@Tag(name = "Salute", description = "API per la gestione della salute delle galline")
public class SaluteController {

    private final SaluteService saluteService;
    private final BearerAuthResolver auth;

    public SaluteController(SaluteService saluteService, BearerAuthResolver auth) {
        this.saluteService = saluteService;
        this.auth = auth;
    }

    @Operation(summary = "Accesso salute",
            description = "Restituisce utente e galline, verificando che il pollaio sia configurato.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accesso riuscito",
                    content = @Content(schema = @Schema(implementation = AccessoSaluteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Header Authorization mancante o malformato",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token non valido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Utente non trovato",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Pollaio non configurato",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/accesso-salute")
    public AccessoSaluteResponse accessoSalute(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return saluteService.accessoSalute(auth.resolveUserId(authorizationHeader));
    }

    @Operation(summary = "Consiglio salute gallina",
            description = "Valuta lo stato di salute di una gallina in base al peso (1500-2500 g).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consiglio restituito",
                    content = @Content(schema = @Schema(implementation = ConsiglioSaluteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Header mancante o ID non valido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token non valido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Utente o gallina non trovati",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Pollaio non configurato",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/consiglio/{idGallina}")
    public ConsiglioSaluteResponse richiediConsiglio(
            @PathVariable Long idGallina,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return saluteService.richiediConsiglio(auth.resolveUserId(authorizationHeader), idGallina);
    }
}