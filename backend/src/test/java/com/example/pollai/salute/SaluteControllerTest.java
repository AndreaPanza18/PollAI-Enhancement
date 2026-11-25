package com.example.pollai.salute;

import com.example.pollai.common.security.BearerAuthResolver;
import com.example.pollai.salute.dto.AccessoSaluteResponse;
import com.example.pollai.salute.dto.ConsiglioSaluteResponse;
import com.example.pollai.salute.dto.StatoSalute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaluteControllerTest {

    @Mock
    private SaluteService saluteService;

    @Mock
    private BearerAuthResolver auth;

    @InjectMocks
    private SaluteController controller;

    @Test
    void accessoSalute_delegaAlService() {
        AccessoSaluteResponse risposta = new AccessoSaluteResponse(null, List.of());
        when(auth.resolveUserId("Bearer x")).thenReturn(1L);
        when(saluteService.accessoSalute(1L)).thenReturn(risposta);

        assertSame(risposta, controller.accessoSalute("Bearer x"));
    }

    @Test
    void richiediConsiglio_delegaAlService() {
        ConsiglioSaluteResponse risposta = new ConsiglioSaluteResponse(StatoSalute.BUONA, "ok");
        when(auth.resolveUserId("Bearer x")).thenReturn(1L);
        when(saluteService.richiediConsiglio(1L, 5L)).thenReturn(risposta);

        assertSame(risposta, controller.richiediConsiglio(5L, "Bearer x"));
    }
}