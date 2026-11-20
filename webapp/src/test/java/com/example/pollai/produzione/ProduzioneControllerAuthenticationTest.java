package com.example.pollai.produzione;

import com.example.pollai.utente.UtenteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProduzioneControllerAuthenticationTest {

    @Mock
    private ProduzioneService produzioneService;

    @Mock
    private UtenteService utenteService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletRequest request;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ProduzioneController controller;

    @Test
    void productionPagesRequireALoggedInUser() throws Exception {
        when(session.getAttribute("user")).thenReturn(null);

        assertEquals("login", controller.produzione(session));
        assertEquals("login", controller.inserisciUova("2025-01-01", 3, request, session));
        assertEquals("login", controller.archivioProduzione("1 month", session, request, redirectAttributes));
        assertEquals("login", controller.predizioneProduzione(
                "1 month", "mangime", 20, session, request, redirectAttributes));

        verifyNoInteractions(produzioneService, utenteService, restTemplate, redirectAttributes);
    }
}
