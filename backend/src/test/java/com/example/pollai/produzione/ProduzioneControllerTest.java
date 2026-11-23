package com.example.pollai.produzione;

import com.example.pollai.pollaio.Gallina;
import com.example.pollai.pollaio.Pollaio;
import com.example.pollai.utente.Utente;
import com.example.pollai.utente.UtenteService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProduzioneControllerTest {

    @Mock
    private ProduzioneService produzioneService;

    @Mock
    private UtenteService utenteService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProduzioneController controller;

    private Utente user;
    private Pollaio pollaio;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        user = new Utente();
        user.setId(17L);
        pollaio = new Pollaio();
        pollaio.setQuantity(2);
        pollaio.setGalline(List.of(new Gallina("Livornese", 1, 2, pollaio)));
        user.setPollaio(pollaio);
        session = new MockHttpSession();
    }

    @Test
    void productionPageRedirectsUnauthenticatedUserToLogin() {
        assertEquals("login", controller.produzione(session));
        verifyNoInteractions(utenteService, produzioneService);
    }

    @Test
    void productionPageRequiresConfiguredCoop() {
        putUserInSession();
        user.setPollaio(null);
        when(utenteService.getUtenteById(17L)).thenReturn(Optional.of(user));
        assertEquals("configura-pollaio", controller.produzione(session));

        pollaio.setQuantity(0);
        user.setPollaio(pollaio);
        assertEquals("configura-pollaio", controller.produzione(session));
    }

    @Test
    void productionPageCreatesMissingProductionAndUpdatesSession() {
        putUserInSession();
        DatiProduzione produzione = new DatiProduzione();
        when(utenteService.getUtenteById(17L)).thenReturn(Optional.of(user));
        when(produzioneService.createProduzione(pollaio)).thenReturn(produzione);

        assertEquals("gestione-produzione", controller.produzione(session));

        assertSame(produzione, pollaio.getProduzione());
        assertSame(user, session.getAttribute("user"));
        verify(produzioneService).createProduzione(pollaio);
    }

    @Test
    void productionPageReusesExistingProduction() {
        putUserInSession();
        DatiProduzione produzione = new DatiProduzione();
        pollaio.setProduzione(produzione);
        when(utenteService.getUtenteById(17L)).thenReturn(Optional.of(user));

        assertEquals("gestione-produzione", controller.produzione(session));
        verifyNoInteractions(produzioneService);
    }

    @Test
    void insertEggsRequiresUserAndRedirectsToRefererOrHome() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertEquals("login", controller.inserisciUova("2026-11-22", 4, request, session));

        putUserInSession();
        DatiProduzione updated = new DatiProduzione();
        when(utenteService.getUtenteById(17L)).thenReturn(Optional.of(user));
        when(produzioneService.aggiornaUovaGiorno(eq(4), any(), eq(LocalDate.of(2026, 11, 22))))
                .thenReturn(updated);

        assertEquals("redirect:/", controller.inserisciUova("2026-11-22", 4, request, session));
        assertSame(updated, pollaio.getProduzione());

        request.addHeader("Referer", "/produzione");
        assertEquals("redirect:/produzione", controller.inserisciUova("2026-11-22", 4, request, session));
    }

    @Test
    void archiveRequiresUserAndSupportsAllPeriodsAndRefererFallback() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();
        assertEquals("login", controller.archivioProduzione("1 month", session, request, attributes));

        putUserInSession();
        when(utenteService.getUtenteById(17L)).thenReturn(Optional.of(user));
        when(produzioneService.getProduzionePeriodo(any(LocalDate.class), any())).thenReturn(42);

        assertEquals("redirect:/", controller.archivioProduzione("3 months", session, request, attributes));
        assertEquals("The eggs produced in the last 3 months are 42", attributes.getFlashAttributes().get("archivio"));
        assertEquals("redirect:/", controller.archivioProduzione("1 month", session, request, attributes));
        assertEquals("redirect:/", controller.archivioProduzione("other", session, request, attributes));

        request.addHeader("Referer", "/archivio");
        assertEquals("redirect:/archivio", controller.archivioProduzione("1 month", session, request, attributes));
        verify(produzioneService, times(4)).getProduzionePeriodo(any(LocalDate.class), any());
    }

    @Test
    void predictionRequiresUserAndScalesRemotePredictionByCategory() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();
        assertEquals("login", controller.predizioneProduzione("1 months", "feed", 20,
                session, request, attributes));

        putUserInSession();
        when(utenteService.getUtenteById(17L)).thenReturn(Optional.of(user));
        when(restTemplate.postForObject(eq("http://uova-service:8050/predizione"),
                any(org.springframework.http.HttpEntity.class), eq(String.class)))
                .thenReturn("{\"prediction\":10}");

        assertEquals("redirect:/", controller.predizioneProduzione("3 months", "grain", 24,
                session, request, attributes));
        assertEquals("The eggs that will be produces in the next 3 months are 30",
                attributes.getFlashAttributes().get("predizione"));
        Gallina gallina = pollaio.getGalline().get(0);
        assertEquals("grain", gallina.getMangime());
        assertEquals(24, gallina.getTemperatura());

        request.addHeader("Referer", "/dashboard");
        assertEquals("redirect:/dashboard", controller.predizioneProduzione("1 months", "pellet", 18,
                session, request, attributes));
        assertEquals("The eggs that will be produces in the next 1 months are 10",
                attributes.getFlashAttributes().get("predizione"));
        assertEquals("redirect:/dashboard", controller.predizioneProduzione("1 year", "pellet", 18,
                session, request, attributes));
        assertEquals("The eggs that will be produces in the next 1 year are 120",
                attributes.getFlashAttributes().get("predizione"));
        assertEquals("redirect:/dashboard", controller.predizioneProduzione("custom", "pellet", 18,
                session, request, attributes));
        verify(restTemplate, times(4)).postForObject(eq("http://uova-service:8050/predizione"),
                any(org.springframework.http.HttpEntity.class), eq(String.class));
    }

    @Test
    void predictionPropagatesMalformedJsonError() throws Exception {
        putUserInSession();
        when(utenteService.getUtenteById(17L)).thenReturn(Optional.of(user));
        when(restTemplate.postForObject(anyString(), any(org.springframework.http.HttpEntity.class), eq(String.class)))
                .thenReturn("invalid-json");

        assertThrows(com.fasterxml.jackson.core.JsonProcessingException.class,
                () -> controller.predizioneProduzione("1 months", "feed", 20, session,
                        new MockHttpServletRequest(), new RedirectAttributesModelMap()));
    }

    private void putUserInSession() {
        session.setAttribute("user", user);
    }
}
