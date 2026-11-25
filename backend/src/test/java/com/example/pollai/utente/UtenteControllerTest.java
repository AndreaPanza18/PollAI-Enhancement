
package com.example.pollai.utente;

import com.example.pollai.common.security.BearerAuthResolver;
import com.example.pollai.utente.dto.LoginRequest;
import com.example.pollai.utente.dto.LoginResponse;
import com.example.pollai.utente.dto.RegisterRequest;
import com.example.pollai.utente.dto.UtenteResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtenteControllerTest {

    @Mock
    private UtenteService utenteService;

    @Mock
    private BearerAuthResolver authResolver;

    private UtenteController createController() {
        return new UtenteController(utenteService, authResolver);
    }

    private final UtenteResponse user = new UtenteResponse(
            1L,
            "Mario",
            "Rossi",
            "3331234567",
            true,
            "IT12345678901",
            "mario.rossi@example.com"
    );

    private final LoginResponse loginResponse = new LoginResponse(
            "jwt-token",
            "Bearer",
            3600L,
            user,
            false
    );

    // ---------------- LOGIN ----------------

    @Test
    void loginReturnsOkWhenServiceAuthenticates() {
        LoginRequest request = new LoginRequest(
                "mario.rossi@example.com",
                "Sup3rm@n!"
        );

        when(utenteService.login(request))
                .thenReturn(Optional.of(loginResponse));

        var response = createController().login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(loginResponse, response.getBody());

        verify(utenteService).login(request);
    }

    @Test
    void loginReturnsUnauthorizedWhenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest(
                "mario.rossi@example.com",
                "password-sbagliata"
        );

        when(utenteService.login(request))
                .thenReturn(Optional.empty());

        var response = createController().login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ---------------- REGISTRAZIONE ----------------

    @Test
    void registerReturnsCreatedWithLoginResponse() {
        RegisterRequest request = new RegisterRequest(
                "Mario",
                "Rossi",
                "3331234567",
                true,
                "IT12345678901",
                "mario.rossi@example.com",
                "Sup3rm@n!"
        );

        when(utenteService.register(request)).thenReturn(loginResponse);

        var response = createController().register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(loginResponse, response.getBody());

        verify(utenteService).register(request);
    }

    @Test
    void registerPropagatesServiceExceptions() {
        RegisterRequest request = new RegisterRequest(
                "Mario",
                "Rossi",
                "3331234567",
                true,
                "IT12345678901",
                "mario.rossi@example.com",
                "Sup3rm@n!"
        );

        ResponseStatusException exception =
                new ResponseStatusException(HttpStatus.BAD_REQUEST);

        when(utenteService.register(request)).thenThrow(exception);

        assertSame(
                exception,
                assertThrows(
                        ResponseStatusException.class,
                        () -> createController().register(request)
                )
        );
    }

    // ---------------- ELENCO UTENTI ----------------

    @Test
    void getUsersReturnsUserDtos() {
        List<UtenteResponse> users = List.of(user);

        when(utenteService.getAllUtentiResponse()).thenReturn(users);

        List<UtenteResponse> result = createController().getUsers();

        assertSame(users, result);
        verify(utenteService).getAllUtentiResponse();
    }

    @Test
    void getUsersReturnsEmptyListWhenNoUsersExist() {
        when(utenteService.getAllUtentiResponse()).thenReturn(List.of());

        assertTrue(createController().getUsers().isEmpty());
    }

    // ---------------- PROFILO ----------------

    @Test
    void profileResolvesUserIdAndReturnsUser() {
        when(authResolver.resolveUserId("Bearer jwt-token"))
                .thenReturn(1L);

        when(utenteService.getProfilo(1L))
                .thenReturn(user);

        UtenteResponse response =
                createController().profilo("Bearer jwt-token");

        assertSame(user, response);

        verify(authResolver).resolveUserId("Bearer jwt-token");
        verify(utenteService).getProfilo(1L);
    }

    @Test
    void profilePropagatesAuthenticationException() {
        ResponseStatusException exception =
                new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Token non valido"
                );

        when(authResolver.resolveUserId("Bearer invalid"))
                .thenThrow(exception);

        assertSame(
                exception,
                assertThrows(
                        ResponseStatusException.class,
                        () -> createController().profilo("Bearer invalid")
                )
        );

        verifyNoInteractions(utenteService);
    }

    @Test
    void profilePropagatesNotFoundFromService() {
        when(authResolver.resolveUserId("Bearer jwt-token"))
                .thenReturn(99L);

        ResponseStatusException exception =
                new ResponseStatusException(HttpStatus.NOT_FOUND);

        when(utenteService.getProfilo(99L)).thenThrow(exception);

        assertSame(
                exception,
                assertThrows(
                        ResponseStatusException.class,
                        () -> createController().profilo("Bearer jwt-token")
                )
        );
    }

    @Test
    void profileDoesNotCallServiceWhenHeaderIsInvalid() {
        ResponseStatusException exception =
                new ResponseStatusException(HttpStatus.BAD_REQUEST);

        when(authResolver.resolveUserId("Basic token"))
                .thenThrow(exception);

        assertThrows(
                ResponseStatusException.class,
                () -> createController().profilo("Basic token")
        );

        verifyNoInteractions(utenteService);
    }

    // ---------------- AREA UTENTE ----------------

    @Test
    void areaUtenteResolvesUserIdAndReturnsUser() {
        when(authResolver.resolveUserId("Bearer jwt-token"))
                .thenReturn(1L);

        when(utenteService.getAreaUtente(1L))
                .thenReturn(user);

        UtenteResponse response =
                createController().areaUtente("Bearer jwt-token");

        assertSame(user, response);

        verify(authResolver).resolveUserId("Bearer jwt-token");
        verify(utenteService).getAreaUtente(1L);
    }

    @Test
    void areaUtentePropagatesAuthenticationException() {
        ResponseStatusException exception =
                new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Token non valido"
                );

        when(authResolver.resolveUserId("Bearer invalid"))
                .thenThrow(exception);

        assertSame(
                exception,
                assertThrows(
                        ResponseStatusException.class,
                        () -> createController().areaUtente("Bearer invalid")
                )
        );

        verifyNoInteractions(utenteService);
    }

    @Test
    void areaUtentePropagatesConflictWhenPollaioIsNotConfigured() {
        when(authResolver.resolveUserId("Bearer jwt-token"))
                .thenReturn(1L);

        ResponseStatusException exception =
                new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Pollaio non configurato"
                );

        when(utenteService.getAreaUtente(1L)).thenThrow(exception);

        assertSame(
                exception,
                assertThrows(
                        ResponseStatusException.class,
                        () -> createController().areaUtente("Bearer jwt-token")
                )
        );
    }

    @Test
    void areaUtentePropagatesNotFoundFromService() {
        when(authResolver.resolveUserId("Bearer jwt-token"))
                .thenReturn(99L);

        ResponseStatusException exception =
                new ResponseStatusException(HttpStatus.NOT_FOUND);

        when(utenteService.getAreaUtente(99L)).thenThrow(exception);

        assertSame(
                exception,
                assertThrows(
                        ResponseStatusException.class,
                        () -> createController().areaUtente("Bearer jwt-token")
                )
        );
    }
}
