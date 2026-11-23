package com.example.pollai.produzione;

import com.example.pollai.pollaio.Pollaio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProduzioneServiceTest {

    @Mock
    private ProduzioneDAO produzioneDAO;

    @InjectMocks
    private ProduzioneService produzioneService;

    @Test
    void createsAndAssociatesProductionWithPollaio() {
        Pollaio pollaio = new Pollaio();
        when(produzioneDAO.save(any(DatiProduzione.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DatiProduzione result = produzioneService.createProduzione(pollaio);

        assertSame(result, pollaio.getProduzione());
        assertSame(pollaio, result.getPollaio());
        verify(produzioneDAO).save(result);
    }

    @Test
    void updatesDailyEggCountAndSavesProduction() {
        DatiProduzione produzione = new DatiProduzione();
        LocalDate date = LocalDate.of(2026, 11, 22);
        when(produzioneDAO.save(produzione)).thenReturn(produzione);

        DatiProduzione result = produzioneService.aggiornaUovaGiorno(8, produzione, date);

        assertSame(produzione, result);
        assertEquals(8, produzione.cercaPerGiorno(date));
        verify(produzioneDAO).save(produzione);
    }

    @Test
    void sumsEggCountsAfterRequestedDate() {
        DatiProduzione produzione = new DatiProduzione();
        produzione.setId(3L);
        LocalDate date = LocalDate.of(2026, 11, 1);
        when(produzioneDAO.findUovaAfterDate(3L, date)).thenReturn(List.of(3, 4, 5));

        assertEquals(12, produzioneService.getProduzionePeriodo(date, produzione));
        verify(produzioneDAO).findUovaAfterDate(3L, date);
    }

    @Test
    void returnsZeroWhenPeriodHasNoProduction() {
        DatiProduzione produzione = new DatiProduzione();
        produzione.setId(3L);
        LocalDate date = LocalDate.of(2026, 11, 1);
        when(produzioneDAO.findUovaAfterDate(3L, date)).thenReturn(List.of());

        assertEquals(0, produzioneService.getProduzionePeriodo(date, produzione));
    }
}
