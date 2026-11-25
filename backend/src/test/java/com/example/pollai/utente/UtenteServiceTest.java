
package com.example.pollai.utente;

import com.example.pollai.pollaio.Pollaio;
import com.example.pollai.utente.dto.LoginRequest;
import com.example.pollai.utente.dto.LoginResponse;
import com.example.pollai.utente.dto.RegisterRequest;
import com.example.pollai.utente.dto.TokenJWTDto;
import com.example.pollai.utente.dto.UtenteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtenteServiceTest {

    @Mock
    private UtenteDAO utenteDAO;

    @Mock
    private TokenJWTService tokenJWTService;

    @InjectMocks
    private UtenteService utenteService;

    private Utente utente;

    private static final String PASSWORD = "Sup3rm@n!";

    @BeforeEach
    void setup() {
        utente = new Utente();
        utente.setId(1L);
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setPhone_number("3331234567");
        utente.setRasvsl(true);
        utente.setPIVA("IT12345678901");
        utente.setEmail("mario.rossi@example.com");
        utente.setPassword(UtenteService.hashPassword(PASSWORD));
    }

    @Test
    void saveUtenteHashesPasswordBeforeSaving() {
        when(utenteDAO.save(utente)).thenReturn(utente);

        Utente saved = utenteService.saveUtente(utente, PASSWORD);

        assertSame(utente, saved);
        assertNotEquals(PASSWORD, saved.getPassword());
        assertTrue(UtenteService.verifyPassword(PASSWORD, saved.getPassword()));

        verify(utenteDAO).save(utente);
    }

    @Test
    void loginReturnsTokenAndPublicUserWhenCredentialsAreValid() {
        Pollaio pollaio = new Pollaio();
        pollaio.setQuantity(2);
        utente.setPollaio(pollaio);

        when(utenteDAO.findByEmail(utente.getEmail()))
                .thenReturn(Optional.of(utente));

        when(tokenJWTService.generateToken("1", utente.getEmail(), "USER"))
                .thenReturn(new TokenJWTDto("jwt-token"));

        when(tokenJWTService.getJwtExpirationSeconds())
                .thenReturn(3600L);

        Optional<LoginResponse> result = utenteService.login(
                new LoginRequest(utente.getEmail(), PASSWORD)
        );

        assertTrue(result.isPresent());

        LoginResponse response = result.get();

        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
        assertTrue(response.pollaioConfigurato());
        assertEquals(utente.getEmail(), response.user().email());

        verify(tokenJWTService).generateToken("1", utente.getEmail(), "USER");
    }

    @Test
    void loginReturnsEmptyWhenEmailDoesNotExist() {
        when(utenteDAO.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        Optional<LoginResponse> result = utenteService.login(
                new LoginRequest("unknown@example.com", "secret")
        );

        assertTrue(result.isEmpty());
        verifyNoInteractions(tokenJWTService);
    }

    @Test
    void loginReturnsEmptyWhenPasswordIsWrong() {
        when(utenteDAO.findByEmail(utente.getEmail()))
                .thenReturn(Optional.of(utente));

        Optional<LoginResponse> result = utenteService.login(
                new LoginRequest(utente.getEmail(), "wrong")
        );

        assertTrue(result.isEmpty());
        verifyNoInteractions(tokenJWTService);
    }

    @Test
    void loginReportsPollaioNotConfiguredWhenPollaioIsNull() {
        stubSuccessfulLogin();

        LoginResponse response = utenteService.login(
                new LoginRequest(utente.getEmail(), PASSWORD)
        ).orElseThrow();

        assertFalse(response.pollaioConfigurato());
    }

    @Test
    void loginReportsPollaioNotConfiguredWhenQuantityIsZero() {
        Pollaio pollaio = new Pollaio();
        pollaio.setQuantity(0);
        utente.setPollaio(pollaio);

        stubSuccessfulLogin();

        LoginResponse response = utenteService.login(
                new LoginRequest(utente.getEmail(), PASSWORD)
        ).orElseThrow();

        assertFalse(response.pollaioConfigurato());
    }

    private void stubSuccessfulLogin() {
        when(utenteDAO.findByEmail(utente.getEmail()))
                .thenReturn(Optional.of(utente));

        when(tokenJWTService.generateToken("1", utente.getEmail(), "USER"))
                .thenReturn(new TokenJWTDto("jwt-token"));

        when(tokenJWTService.getJwtExpirationSeconds())
                .thenReturn(3600L);
    }

    @Test
    void registerCreatesUserHashesPasswordAndReturnsLoginResponse() {
        when(utenteDAO.save(any(Utente.class))).thenAnswer(invocation -> {
            Utente toSave = invocation.getArgument(0);
            toSave.setId(5L);
            return toSave;
        });

        when(tokenJWTService.generateToken("5", "mario.rossi@example.com", "USER"))
                .thenReturn(new TokenJWTDto("registered-token"));

        when(tokenJWTService.getJwtExpirationSeconds())
                .thenReturn(1800L);

        LoginResponse response = utenteService.register(
                new RegisterRequest(
                        "Mario",
                        "Rossi",
                        "3331234567",
                        true,
                        "IT12345678901",
                        "mario.rossi@example.com",
                        PASSWORD
                )
        );

        assertEquals("registered-token", response.accessToken());
        assertEquals(1800L, response.expiresIn());
        assertEquals(5L, response.user().id());
        assertEquals("Mario", response.user().nome());

        assertTrue(UtenteService.verifyPassword(
                PASSWORD,
                captureSavedPasswordUser()
        ));
    }

    @Test
    void registerCopiesEveryFieldFromRequestToSavedEntity() {
        when(utenteDAO.save(any(Utente.class))).thenAnswer(invocation -> {
            Utente toSave = invocation.getArgument(0);
            toSave.setId(7L);
            return toSave;
        });

        when(tokenJWTService.generateToken("7", "luigi@example.com", "USER"))
                .thenReturn(new TokenJWTDto("token"));

        when(tokenJWTService.getJwtExpirationSeconds())
                .thenReturn(60L);

        LoginResponse response = utenteService.register(
                new RegisterRequest(
                        "Luigi",
                        "Verdi",
                        "3401112223",
                        false,
                        "IT99999999999",
                        "luigi@example.com",
                        PASSWORD
                )
        );

        ArgumentCaptor<Utente> captor =
                ArgumentCaptor.forClass(Utente.class);

        verify(utenteDAO).save(captor.capture());

        Utente saved = captor.getValue();

        assertEquals("Luigi", saved.getNome());
        assertEquals("Verdi", saved.getCognome());
        assertEquals("3401112223", saved.getPhone_number());
        assertEquals(Boolean.FALSE, saved.getRasvsl());
        assertEquals("IT99999999999", saved.getPIVA());
        assertEquals("luigi@example.com", saved.getEmail());

        assertNotEquals(PASSWORD, saved.getPassword());

        assertFalse(response.pollaioConfigurato());
        assertEquals("Bearer", response.tokenType());
        assertEquals(60L, response.expiresIn());
    }

    @Test
    void registerPropagatesDaoFailureWithoutIssuingToken() {
        when(utenteDAO.save(any(Utente.class)))
                .thenThrow(new IllegalStateException("email duplicata"));

        assertThrows(
                IllegalStateException.class,
                () -> utenteService.register(
                        new RegisterRequest(
                                "Mario",
                                "Rossi",
                                "3331234567",
                                true,
                                "IT12345678901",
                                "mario.rossi@example.com",
                                PASSWORD
                        )
                )
        );

        verifyNoInteractions(tokenJWTService);
    }

    private String captureSavedPasswordUser() {
        ArgumentCaptor<Utente> captor =
                ArgumentCaptor.forClass(Utente.class);

        verify(utenteDAO).save(captor.capture());

        return captor.getValue().getPassword();
    }

    @Test
    void getProfiloLoadsUserUsingAuthenticatedUserId() {
        when(utenteDAO.findById(1L))
                .thenReturn(Optional.of(utente));

        UtenteResponse response = utenteService.getProfilo(1L);

        assertEquals(1L, response.id());
        assertEquals(utente.getEmail(), response.email());

        verify(utenteDAO).findById(1L);
        verifyNoInteractions(tokenJWTService);
    }

    @Test
    void getProfiloThrowsNotFoundWhenUserDoesNotExist() {
        when(utenteDAO.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> utenteService.getProfilo(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(utenteDAO).findById(99L);
        verifyNoInteractions(tokenJWTService);
    }

    @Test
    void areaUtenteRequiresConfiguredPollaio() {
        when(utenteDAO.findById(1L))
                .thenReturn(Optional.of(utente));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> utenteService.getAreaUtente(1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(utenteDAO).findById(1L);
        verifyNoInteractions(tokenJWTService);
    }

    @Test
    void areaUtenteReturnsUserWhenPollaioHasAtLeastOneHen() {
        Pollaio pollaio = new Pollaio();
        pollaio.setQuantity(1);
        utente.setPollaio(pollaio);

        when(utenteDAO.findById(1L))
                .thenReturn(Optional.of(utente));

        UtenteResponse response = utenteService.getAreaUtente(1L);

        assertEquals(utente.getEmail(), response.email());
        assertEquals(1L, response.id());

        verify(utenteDAO).findById(1L);
        verifyNoInteractions(tokenJWTService);
    }

    @Test
    void getAreaUtenteThrowsNotFoundWhenUserDoesNotExist() {
        when(utenteDAO.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> utenteService.getAreaUtente(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(utenteDAO).findById(99L);
        verifyNoInteractions(tokenJWTService);
    }

    @Test
    void getAreaUtenteThrowsConflictWhenPollaioHasZeroHens() {
        Pollaio pollaio = new Pollaio();
        pollaio.setQuantity(0);
        utente.setPollaio(pollaio);

        when(utenteDAO.findById(1L))
                .thenReturn(Optional.of(utente));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> utenteService.getAreaUtente(1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(utenteDAO).findById(1L);
        verifyNoInteractions(tokenJWTService);
    }

    @Test
    void getAllUtentiResponseMapsEntitiesToPublicDtos() {
        when(utenteDAO.findAll())
                .thenReturn(List.of(utente));

        List<UtenteResponse> result =
                utenteService.getAllUtentiResponse();

        assertEquals(1, result.size());
        assertEquals(utente.getId(), result.get(0).id());
        assertEquals(utente.getEmail(), result.get(0).email());

        verify(utenteDAO).findAll();
    }

    @Test
    void getAllUtentiResponseReturnsEmptyListWhenNoUsers() {
        when(utenteDAO.findAll())
                .thenReturn(List.of());

        assertTrue(utenteService.getAllUtentiResponse().isEmpty());
    }

    @Test
    void getAllUtentiReturnsDaoResult() {
        when(utenteDAO.findAll())
                .thenReturn(List.of(utente));

        assertEquals(
                List.of(utente),
                utenteService.getAllUtenti()
        );

        verify(utenteDAO).findAll();
    }

    @Test
    void getUtenteByIdAndDeleteDelegateToDao() {
        when(utenteDAO.findById(1L))
                .thenReturn(Optional.of(utente));

        assertTrue(utenteService.getUtenteById(1L).isPresent());

        utenteService.deleteUtente(1L);

        verify(utenteDAO).findById(1L);
        verify(utenteDAO).deleteById(1L);
    }

    @Test
    void getUtenteByIdReturnsEmptyWhenMissing() {
        when(utenteDAO.findById(404L))
                .thenReturn(Optional.empty());

        assertTrue(utenteService.getUtenteById(404L).isEmpty());

        verify(utenteDAO).findById(404L);
    }

    @Test
    void autenticazioneReturnsUserWhenPasswordMatches() {
        when(utenteDAO.findByEmail(utente.getEmail()))
                .thenReturn(Optional.of(utente));

        Optional<Utente> result =
                utenteService.autenticazione(utente.getEmail(), PASSWORD);

        assertTrue(result.isPresent());
        assertSame(utente, result.get());
    }

    @Test
    void autenticazioneReturnsEmptyWhenPasswordDoesNotMatch() {
        when(utenteDAO.findByEmail(utente.getEmail()))
                .thenReturn(Optional.of(utente));

        Optional<Utente> result =
                utenteService.autenticazione(utente.getEmail(), "wrong");

        assertTrue(result.isEmpty());
    }

    @Test
    void autenticazioneReturnsEmptyWhenEmailIsUnknown() {
        when(utenteDAO.findByEmail("nobody@example.com"))
                .thenReturn(Optional.empty());

        Optional<Utente> result =
                utenteService.autenticazione("nobody@example.com", PASSWORD);

        assertTrue(result.isEmpty());
    }

    @Test
    void hashPasswordIsSaltedAndVerifiable() {
        String first = UtenteService.hashPassword(PASSWORD);
        String second = UtenteService.hashPassword(PASSWORD);

        assertNotEquals(first, second);

        assertTrue(UtenteService.verifyPassword(PASSWORD, first));
        assertTrue(UtenteService.verifyPassword(PASSWORD, second));

        assertFalse(UtenteService.verifyPassword("altra-password", first));
    }
}
