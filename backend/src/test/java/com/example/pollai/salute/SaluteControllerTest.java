package com.example.pollai.salute;

import com.example.pollai.pollaio.Gallina;
import com.example.pollai.pollaio.Pollaio;
import com.example.pollai.utente.TokenJWTService;
import com.example.pollai.utente.Utente;
import com.example.pollai.utente.UtenteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaluteControllerTest {

    @InjectMocks
    private SaluteController saluteController;

    @Mock
    private UtenteService utenteService;

    @Mock
    private TokenJWTService tokenJWTService;

    private static final String VALID_TOKEN = "valid-jwt-token";
    private static final String AUTH_HEADER = "Bearer " + VALID_TOKEN;

    private Utente utente;

    private Pollaio pollaio;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        pollaio = new Pollaio();
        utente = new Utente();
        utente.setId(1L);
        utente.setNome("User");
        utente.setPollaio(pollaio);
        pollaio.setUtente(utente);
        pollaio.setGalline(new ArrayList<>());

        when(tokenJWTService.extractUserId(VALID_TOKEN)).thenReturn("1");
        when(utenteService.getUtenteById(anyLong())).thenReturn(Optional.of(utente));
    }

    // ---- Test per GET /accesso-salute ----

    @Test
    void testAccessoSalute_success() {
        pollaio.setQuantity(1);

        ResponseEntity<Map<String, Object>> response = saluteController.accessoSalute(AUTH_HEADER);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("utente"));
        assertTrue(response.getBody().containsKey("galline"));
    }

    @Test
    void testAccessoSalute_missingHeader() {
        ResponseEntity<Map<String, Object>> response = saluteController.accessoSalute(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAccessoSalute_invalidHeader() {
        ResponseEntity<Map<String, Object>> response = saluteController.accessoSalute("InvalidFormat");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAccessoSalute_pollaioNotConfigured() {
        pollaio.setQuantity(0);

        ResponseEntity<Map<String, Object>> response = saluteController.accessoSalute(AUTH_HEADER);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testAccessoSalute_pollaioNull() {
        utente.setPollaio(null);

        ResponseEntity<Map<String, Object>> response = saluteController.accessoSalute(AUTH_HEADER);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // ---- Test per POST /consiglio/{idGallina} ----

    @Test
    void testRichiediConsiglio_healthyWeight() {
        Pollaio pollaio = mock(Pollaio.class);
        utente.setPollaio(pollaio);
        when(pollaio.getGalline()).thenReturn(new ArrayList<>());

        Gallina gallina = new Gallina();
        gallina.setPeso(2000);
        when(pollaio.getGallinaByid(1)).thenReturn(Optional.of(gallina));

        ResponseEntity<Map<String, String>> response = saluteController.richiediConsiglio("1", AUTH_HEADER);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("The hen is in good health.", response.getBody().get("message"));
    }

    @Test
    void testRichiediConsiglio_unhealthyWeight() {
        Pollaio pollaio = mock(Pollaio.class);
        utente.setPollaio(pollaio);
        when(pollaio.getGalline()).thenReturn(new ArrayList<>());

        Gallina gallina = new Gallina();
        gallina.setPeso(1000);
        when(pollaio.getGallinaByid(1)).thenReturn(Optional.of(gallina));

        ResponseEntity<Map<String, String>> response = saluteController.richiediConsiglio("1", AUTH_HEADER);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("The hen is not in good health.", response.getBody().get("message"));
    }

    @Test
    void testRichiediConsiglio_overweight() {
        Pollaio pollaio = mock(Pollaio.class);
        utente.setPollaio(pollaio);
        when(pollaio.getGalline()).thenReturn(new ArrayList<>());

        Gallina gallina = new Gallina();
        gallina.setPeso(3000);
        when(pollaio.getGallinaByid(1)).thenReturn(Optional.of(gallina));

        ResponseEntity<Map<String, String>> response = saluteController.richiediConsiglio("1", AUTH_HEADER);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("The hen is not in good health.", response.getBody().get("message"));
    }

    @Test
    void testRichiediConsiglio_henNotFound() {
        Pollaio pollaio = mock(Pollaio.class);
        utente.setPollaio(pollaio);
        when(pollaio.getGalline()).thenReturn(new ArrayList<>());
        when(pollaio.getGallinaByid(1)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, String>> response = saluteController.richiediConsiglio("1", AUTH_HEADER);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Hen with ID 1 not found.", response.getBody().get("message"));
    }

    @Test
    void testRichiediConsiglio_missingHeader() {
        ResponseEntity<Map<String, String>> response = saluteController.richiediConsiglio("1", null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}