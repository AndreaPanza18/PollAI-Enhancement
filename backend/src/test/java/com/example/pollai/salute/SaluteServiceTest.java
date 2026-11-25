package com.example.pollai.salute;

import com.example.pollai.pollaio.Gallina;
import com.example.pollai.pollaio.Pollaio;
import com.example.pollai.salute.dto.AccessoSaluteResponse;
import com.example.pollai.salute.dto.StatoSalute;
import com.example.pollai.utente.Utente;
import com.example.pollai.utente.UtenteDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaluteServiceTest {

    @Mock
    private UtenteDAO utenteDAO;

    @InjectMocks
    private SaluteService service;

    private Utente utenteSenzaPollaio() {
        Utente u = new Utente();
        u.setId(1L);
        return u;
    }

    private Utente utenteConGallina(int peso) {
        Pollaio pollaio = new Pollaio();
        Gallina g = new Gallina("Livorno", 2, peso, pollaio);
        g.setId(1L);
        pollaio.addGallina(g);
        Utente u = utenteSenzaPollaio();
        u.setPollaio(pollaio);
        return u;
    }

    private void assertStatus(HttpStatus atteso, ResponseStatusException ex) {
        assertEquals(atteso, ex.getStatusCode());
    }

    // ---- accessoSalute ----

    @Test
    void accessoSalute_ok() {
        when(utenteDAO.findByIdWithPollaio(1L)).thenReturn(Optional.of(utenteConGallina(2000)));

        AccessoSaluteResponse risposta = service.accessoSalute(1L);

        assertEquals(1L, risposta.utente().id());
        assertEquals(1, risposta.galline().size());
        assertEquals("Livorno", risposta.galline().get(0).razza());
        assertEquals(2000, risposta.galline().get(0).peso());
    }

    @Test
    void accessoSalute_pollaioNull_409() {
        when(utenteDAO.findByIdWithPollaio(1L)).thenReturn(Optional.of(utenteSenzaPollaio()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.accessoSalute(1L));
        assertStatus(HttpStatus.CONFLICT, ex);
    }

    @Test
    void accessoSalute_pollaioVuoto_409() {
        Utente u = utenteSenzaPollaio();
        u.setPollaio(new Pollaio());
        when(utenteDAO.findByIdWithPollaio(1L)).thenReturn(Optional.of(u));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.accessoSalute(1L));
        assertStatus(HttpStatus.CONFLICT, ex);
    }

    @Test
    void accessoSalute_utenteInesistente_404() {
        when(utenteDAO.findByIdWithPollaio(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.accessoSalute(1L));
        assertStatus(HttpStatus.NOT_FOUND, ex);
    }

    // ---- richiediConsiglio ----

    @ParameterizedTest
    @CsvSource({"1499,ATTENZIONE", "1500,BUONA", "2500,BUONA", "2501,ATTENZIONE"})
    void richiediConsiglio_soglieDiPeso(int peso, StatoSalute atteso) {
        when(utenteDAO.findByIdWithPollaio(1L)).thenReturn(Optional.of(utenteConGallina(peso)));

        assertEquals(atteso, service.richiediConsiglio(1L, 1L).stato());
    }

    @Test
    void richiediConsiglio_messaggi() {
        when(utenteDAO.findByIdWithPollaio(1L))
                .thenReturn(Optional.of(utenteConGallina(2000)))
                .thenReturn(Optional.of(utenteConGallina(1000)));

        assertEquals("The hen is in good health.", service.richiediConsiglio(1L, 1L).message());
        assertEquals("The hen is not in good health.", service.richiediConsiglio(1L, 1L).message());
    }

    @Test
    void richiediConsiglio_gallinaInesistente_404() {
        when(utenteDAO.findByIdWithPollaio(1L)).thenReturn(Optional.of(utenteConGallina(2000)));

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.richiediConsiglio(1L, 99L));
        assertStatus(HttpStatus.NOT_FOUND, ex);
    }

    @Test
    void richiediConsiglio_pollaioNull_409() {
        when(utenteDAO.findByIdWithPollaio(1L)).thenReturn(Optional.of(utenteSenzaPollaio()));

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.richiediConsiglio(1L, 1L));
        assertStatus(HttpStatus.CONFLICT, ex);
    }

    @Test
    void richiediConsiglio_utenteInesistente_404() {
        when(utenteDAO.findByIdWithPollaio(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.richiediConsiglio(1L, 1L));
        assertStatus(HttpStatus.NOT_FOUND, ex);
    }
}