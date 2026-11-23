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
        utente.setPassword(UtenteService.hashPassword("Sup3rm@n!"));
    }

    @Test
    void saveUtenteHashesPasswordBeforeSaving() {
        when(utenteDAO.save(utente)).thenReturn(utente);

        Utente saved = utenteService.saveUtente(utente, "Sup3rm@n!");

        assertSame(utente, saved);
        assertNotEquals("Sup3rm@n!", saved.getPassword());
        assertTrue(UtenteService.verifyPassword("Sup3rm@n!", saved.getPassword()));
        verify(utenteDAO).save(utente);
    }

    @Test
    void loginReturnsTokenAndPublicUserWhenCredentialsAreValid() {
        Pollaio pollaio = new Pollaio();
        pollaio.setQuantity(2);
        utente.setPollaio(pollaio);
        when(utenteDAO.findByEmail(utente.getEmail())).thenReturn(Optional.of(utente));
        when(tokenJWTService.generateToken("1", utente.getEmail(), "USER"))
                .thenReturn(new TokenJWTDto("jwt-token"));
        when(tokenJWTService.getJwtExpirationSeconds()).thenReturn(3600L);

        Optional<LoginResponse> result = utenteService.login(new LoginRequest(utente.getEmail(), "Sup3rm@n!"));

        assertTrue(result.isPresent());
        assertEquals("jwt-token", result.get().accessToken());
        assertEquals("Bearer", result.get().tokenType());
        assertEquals(3600L, result.get().expiresIn());
        assertTrue(result.get().pollaioConfigurato());
        assertEquals("mario.rossi@example.com", result.get().user().email());
        verify(tokenJWTService).generateToken("1", utente.getEmail(), "USER");
    }

    @Test
    void loginReturnsEmptyWhenEmailDoesNotExist() {
        when(utenteDAO.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<LoginResponse> result = utenteService.login(new LoginRequest("unknown@example.com", "secret"));

        assertTrue(result.isEmpty());
        verifyNoInteractions(tokenJWTService);
    }

    @Test
    void loginReturnsEmptyWhenPasswordIsWrong() {
        when(utenteDAO.findByEmail(utente.getEmail())).thenReturn(Optional.of(utente));

        Optional<LoginResponse> result = utenteService.login(new LoginRequest(utente.getEmail(), "wrong"));

        assertTrue(result.isEmpty());
        verifyNoInteractions(tokenJWTService);
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
        when(tokenJWTService.getJwtExpirationSeconds()).thenReturn(1800L);

        LoginResponse response = utenteService.register(new RegisterRequest(
                "Mario", "Rossi", "3331234567", true, "IT12345678901",
                "mario.rossi@example.com", "Sup3rm@n!"));

        assertEquals("registered-token", response.accessToken());
        assertEquals(1800L, response.expiresIn());
        assertEquals(5L, response.user().id());
        assertEquals("Mario", response.user().nome());
        assertTrue(UtenteService.verifyPassword("Sup3rm@n!", captureSavedPasswordUser()));
    }

    private String captureSavedPasswordUser() {
        org.mockito.ArgumentCaptor<Utente> captor = org.mockito.ArgumentCaptor.forClass(Utente.class);
        verify(utenteDAO).save(captor.capture());
        return captor.getValue().getPassword();
    }

    @Test
    void getAllUtentiResponseMapsEntitiesToPublicDtos() {
        when(utenteDAO.findAll()).thenReturn(List.of(utente));

        List<UtenteResponse> result = utenteService.getAllUtentiResponse();

        assertEquals(1, result.size());
        assertEquals(utente.getId(), result.get(0).id());
        assertEquals(utente.getEmail(), result.get(0).email());
        verify(utenteDAO).findAll();
    }

    @Test
    void getProfiloLoadsUserFromTokenSubject() {
        when(tokenJWTService.extractUserId("jwt-token")).thenReturn("1");
        when(utenteDAO.findById(1L)).thenReturn(Optional.of(utente));

        UtenteResponse response = utenteService.getProfilo("jwt-token");

        assertEquals(1L, response.id());
        assertEquals(utente.getEmail(), response.email());
    }

    @Test
    void profileThrowsNotFoundWhenTokenUserDoesNotExist() {
        when(tokenJWTService.extractUserId("jwt-token")).thenReturn("99");
        when(utenteDAO.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> utenteService.getProfilo("jwt-token"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void areaUtenteRequiresConfiguredPollaio() {
        when(tokenJWTService.extractUserId("jwt-token")).thenReturn("1");
        when(utenteDAO.findById(1L)).thenReturn(Optional.of(utente));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> utenteService.getAreaUtente("jwt-token"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void areaUtenteReturnsUserWhenPollaioHasAtLeastOneHen() {
        Pollaio pollaio = new Pollaio();
        pollaio.setQuantity(1);
        utente.setPollaio(pollaio);
        when(tokenJWTService.extractUserId("jwt-token")).thenReturn("1");
        when(utenteDAO.findById(1L)).thenReturn(Optional.of(utente));

        UtenteResponse response = utenteService.getAreaUtente("jwt-token");

        assertEquals(utente.getEmail(), response.email());
    }

    @Test
    void getUtenteByIdAndDeleteDelegateToDao() {
        when(utenteDAO.findById(1L)).thenReturn(Optional.of(utente));

        assertTrue(utenteService.getUtenteById(1L).isPresent());
        utenteService.deleteUtente(1L);

        verify(utenteDAO).findById(1L);
        verify(utenteDAO).deleteById(1L);
    }

    // ===== Casi aggiuntivi per la copertura =====

    private static final String PASSWORD = "Sup3rm@n!";

    @Test
    void loginReportsPollaioNotConfiguredWhenPollaioIsNull() {
        stubSuccessfulLogin();

        LoginResponse response = utenteService.login(new LoginRequest(utente.getEmail(), PASSWORD)).orElseThrow();

        assertFalse(response.pollaioConfigurato());
    }

    @Test
    void loginReportsPollaioNotConfiguredWhenQuantityIsZero() {
        Pollaio pollaio = new Pollaio();
        pollaio.setQuantity(0);
        utente.setPollaio(pollaio);
        stubSuccessfulLogin();

        LoginResponse response = utenteService.login(new LoginRequest(utente.getEmail(), PASSWORD)).orElseThrow();

        assertFalse(response.pollaioConfigurato());
    }

    private void stubSuccessfulLogin() {
        when(utenteDAO.findByEmail(utente.getEmail())).thenReturn(Optional.of(utente));
        when(tokenJWTService.generateToken("1", utente.getEmail(), "USER"))
                .thenReturn(new TokenJWTDto("jwt-token"));
        when(tokenJWTService.getJwtExpirationSeconds()).thenReturn(3600L);
    }

    @Test
    void autenticazioneReturnsUserWhenPasswordMatches() {
        when(utenteDAO.findByEmail(utente.getEmail())).thenReturn(Optional.of(utente));

        Optional<Utente> result = utenteService.autenticazione(utente.getEmail(), PASSWORD);

        assertTrue(result.isPresent());
        assertSame(utente, result.get());
    }

    @Test
    void autenticazioneReturnsEmptyWhenPasswordDoesNotMatch() {
        when(utenteDAO.findByEmail(utente.getEmail())).thenReturn(Optional.of(utente));

        assertTrue(utenteService.autenticazione(utente.getEmail(), "wrong").isEmpty());
    }

    @Test
    void autenticazioneReturnsEmptyWhenEmailIsUnknown() {
        when(utenteDAO.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertTrue(utenteService.autenticazione("nobody@example.com", PASSWORD).isEmpty());
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
        when(tokenJWTService.getJwtExpirationSeconds()).thenReturn(60L);

        LoginResponse response = utenteService.register(new RegisterRequest(
                "Luigi", "Verdi", "3401112223", false, "IT99999999999",
                "luigi@example.com", PASSWORD));

        org.mockito.ArgumentCaptor<Utente> captor = org.mockito.ArgumentCaptor.forClass(Utente.class);
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
        when(utenteDAO.save(any(Utente.class))).thenThrow(new IllegalStateException("email duplicata"));

        assertThrows(IllegalStateException.class, () -> utenteService.register(new RegisterRequest(
                "Mario", "Rossi", "3331234567", true, "IT12345678901",
                "mario.rossi@example.com", PASSWORD)));

        verifyNoInteractions(tokenJWTService);
    }

    @Test
    void getProfiloRejectsNonNumericSubject() {
        when(tokenJWTService.extractUserId("jwt-token")).thenReturn("abc");

        // NumberFormatException estende IllegalArgumentException: il controller la traduce in 401
        assertThrows(IllegalArgumentException.class, () -> utenteService.getProfilo("jwt-token"));
        verifyNoInteractions(utenteDAO);
    }

    @Test
    void getProfiloPropagatesJwtException() {
        when(tokenJWTService.extractUserId("bad")).thenThrow(new io.jsonwebtoken.JwtException("invalid"));

        assertThrows(io.jsonwebtoken.JwtException.class, () -> utenteService.getProfilo("bad"));
        verifyNoInteractions(utenteDAO);
    }

    @Test
    void getAreaUtenteThrowsNotFoundWhenUserDoesNotExist() {
        when(tokenJWTService.extractUserId("jwt-token")).thenReturn("99");
        when(utenteDAO.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> utenteService.getAreaUtente("jwt-token"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getAreaUtenteThrowsConflictWhenPollaioHasZeroHens() {
        Pollaio pollaio = new Pollaio();
        pollaio.setQuantity(0);
        utente.setPollaio(pollaio);
        when(tokenJWTService.extractUserId("jwt-token")).thenReturn("1");
        when(utenteDAO.findById(1L)).thenReturn(Optional.of(utente));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> utenteService.getAreaUtente("jwt-token"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void getAllUtentiResponseReturnsEmptyListWhenNoUsers() {
        when(utenteDAO.findAll()).thenReturn(List.of());

        assertTrue(utenteService.getAllUtentiResponse().isEmpty());
    }

    @Test
    void getAllUtentiReturnsDaoResult() {
        when(utenteDAO.findAll()).thenReturn(List.of(utente));

        assertEquals(List.of(utente), utenteService.getAllUtenti());
    }

    @Test
    void getUtenteByIdReturnsEmptyWhenMissing() {
        when(utenteDAO.findById(404L)).thenReturn(Optional.empty());

        assertTrue(utenteService.getUtenteById(404L).isEmpty());
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
