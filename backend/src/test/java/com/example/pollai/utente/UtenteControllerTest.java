package com.example.pollai.utente;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.server.ResponseStatusException;
import com.example.pollai.utente.dto.LoginRequest;
import com.example.pollai.utente.dto.LoginResponse;
import com.example.pollai.utente.dto.RegisterRequest;
import com.example.pollai.utente.dto.UtenteResponse;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtenteControllerTest {

    @Mock
    private UtenteService utenteService;

    @InjectMocks
    private UtenteController utenteController;

    private final LoginResponse loginResponse = new LoginResponse(
            "jwt-token", "Bearer", 3600L,
            new UtenteResponse(1L, "Mario", "Rossi", "3331234567", true,
                    "IT12345678901", "mario.rossi@example.com"),
            false);

    @Test
    void loginReturnsBadRequestForNullBody() {
        ResponseEntity<LoginResponse> response = utenteController.login(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(utenteService);
    }

    @Test
    void loginReturnsOkWhenServiceAuthenticates() {
        LoginRequest request = new LoginRequest("mario.rossi@example.com", "secret");
        when(utenteService.login(request)).thenReturn(Optional.of(loginResponse));

        ResponseEntity<LoginResponse> response = utenteController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(loginResponse, response.getBody());
    }

    @Test
    void loginReturnsUnauthorizedWhenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest("mario.rossi@example.com", "wrong");
        when(utenteService.login(request)).thenReturn(Optional.empty());

        ResponseEntity<LoginResponse> response = utenteController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void registerReturnsBadRequestForNullBody() {
        ResponseEntity<LoginResponse> response = utenteController.register(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(utenteService);
    }

    @Test
    void registerReturnsCreatedWithLoginResponse() {
        RegisterRequest request = new RegisterRequest("Mario", "Rossi", "3331234567", true,
                "IT12345678901", "mario.rossi@example.com", "secret");
        when(utenteService.register(request)).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = utenteController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(loginResponse, response.getBody());
    }

    @Test
    void getUsersReturnsPublicUserDtos() {
        List<UtenteResponse> users = List.of(loginResponse.user());
        when(utenteService.getAllUtentiResponse()).thenReturn(users);

        assertSame(users, utenteController.getUsers());
        verify(utenteService).getAllUtentiResponse();
    }

    @Test
    void profileRejectsMissingOrMalformedAuthorizationHeader() {
        assertEquals(HttpStatus.BAD_REQUEST, utenteController.profilo(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, utenteController.profilo("Basic token").getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, utenteController.profilo("Bearer   ").getStatusCode());
        verifyNoInteractions(utenteService);
    }

    @Test
    void profilePassesBearerTokenAndReturnsUser() {
        when(utenteService.getProfilo("jwt-token")).thenReturn(loginResponse.user());

        ResponseEntity<UtenteResponse> response = utenteController.profilo("Bearer jwt-token ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(loginResponse.user(), response.getBody());
        verify(utenteService).getProfilo("jwt-token");
    }

    @Test
    void profileReturnsUnauthorizedForInvalidJwtOrSubject() {
        when(utenteService.getProfilo("bad-token")).thenThrow(new JwtException("invalid"));

        assertEquals(HttpStatus.UNAUTHORIZED,
                utenteController.profilo("Bearer bad-token").getStatusCode());
        when(utenteService.getProfilo("bad-subject")).thenThrow(new IllegalArgumentException("invalid subject"));
        assertEquals(HttpStatus.UNAUTHORIZED,
                utenteController.profilo("Bearer bad-subject").getStatusCode());
    }

    @Test
    void profileDoesNotTranslateNotFoundStatusFromService() {
        org.springframework.web.server.ResponseStatusException notFound =
                new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND);
        when(utenteService.getProfilo("known-token")).thenThrow(notFound);

        assertSame(notFound, assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> utenteController.profilo("Bearer known-token")));
    }

    @Test
    void areaUtenteValidatesHeaderAndReturnsUser() {
        assertEquals(HttpStatus.BAD_REQUEST, utenteController.areaUtente("Token jwt-token").getStatusCode());
        when(utenteService.getAreaUtente("jwt-token")).thenReturn(loginResponse.user());

        ResponseEntity<UtenteResponse> response = utenteController.areaUtente("Bearer jwt-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(loginResponse.user(), response.getBody());
    }

    @Test
    void areaUtenteReturnsUnauthorizedForInvalidJwtAndPropagatesServiceStatuses() {
        when(utenteService.getAreaUtente("invalid")).thenThrow(new JwtException("invalid"));
        assertEquals(HttpStatus.UNAUTHORIZED,
                utenteController.areaUtente("Bearer invalid").getStatusCode());

        org.springframework.web.server.ResponseStatusException conflict =
                new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT);
        when(utenteService.getAreaUtente("unconfigured")).thenThrow(conflict);
        assertSame(conflict, assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> utenteController.areaUtente("Bearer unconfigured")));
    }

    // ===== Casi aggiuntivi per la copertura =====

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Bearer", "Bearer ", "Bearer     ", "bearer abc", "Basic abc", "abc"})
    void profiloReturnsBadRequestForInvalidAuthorizationHeader(String header) {
        ResponseEntity<UtenteResponse> response = utenteController.profilo(header);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(utenteService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Bearer", "Bearer ", "Bearer     ", "bearer abc", "Basic abc", "abc"})
    void areaUtenteReturnsBadRequestForInvalidAuthorizationHeader(String header) {
        ResponseEntity<UtenteResponse> response = utenteController.areaUtente(header);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(utenteService);
    }

    @Test
    void areaUtenteTrimsWhitespaceAroundBearerToken() {
        when(utenteService.getAreaUtente("jwt-token")).thenReturn(loginResponse.user());

        ResponseEntity<UtenteResponse> response = utenteController.areaUtente("Bearer    jwt-token   ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(loginResponse.user(), response.getBody());
        verify(utenteService).getAreaUtente("jwt-token");
    }

    @Test
    void areaUtenteReturnsUnauthorizedForInvalidSubject() {
        when(utenteService.getAreaUtente("bad-subject")).thenThrow(new IllegalArgumentException("subject"));

        ResponseEntity<UtenteResponse> response = utenteController.areaUtente("Bearer bad-subject");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void areaUtentePropagatesNotFoundFromService() {
        ResponseStatusException notFound = new ResponseStatusException(HttpStatus.NOT_FOUND);
        when(utenteService.getAreaUtente("unknown-user")).thenThrow(notFound);

        assertSame(notFound, assertThrows(ResponseStatusException.class,
                () -> utenteController.areaUtente("Bearer unknown-user")));
    }

    @Test
    void profiloUnauthorizedResponseHasNoBody() {
        when(utenteService.getProfilo("expired")).thenThrow(new JwtException("expired"));

        ResponseEntity<UtenteResponse> response = utenteController.profilo("Bearer expired");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void registerPropagatesServiceExceptions() {
        RegisterRequest request = new RegisterRequest("Mario", "Rossi", "3331234567", true,
                "IT12345678901", "mario.rossi@example.com", "secret");
        ResponseStatusException bad = new ResponseStatusException(HttpStatus.BAD_REQUEST);
        when(utenteService.register(request)).thenThrow(bad);

        assertSame(bad, assertThrows(ResponseStatusException.class, () -> utenteController.register(request)));
    }

    @Test
    void getUsersReturnsEmptyListWhenNoUsers() {
        when(utenteService.getAllUtentiResponse()).thenReturn(List.of());

        assertTrue(utenteController.getUsers().isEmpty());
    }
}
