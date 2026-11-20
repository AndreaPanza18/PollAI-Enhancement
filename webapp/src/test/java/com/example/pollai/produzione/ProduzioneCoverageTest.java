package com.example.pollai.produzione;

import com.example.pollai.pollaio.Pollaio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProduzioneCoverageTest {

    @Mock
    private ProduzioneDAO produzioneDAO;

    @InjectMocks
    private ProduzioneService produzioneService;

    @Test
    void predictionStoresAndReturnsTheValue() {
        Prediction prediction = new Prediction();

        prediction.setPrediction(42);

        assertEquals(42, prediction.getPrediction());
    }

    @Test
    void productionStoresDailyEggCountsAndReturnsThemInDateOrder() {
        DatiProduzione production = new DatiProduzione();
        LocalDate earlierDate = LocalDate.of(2025, 1, 1);
        LocalDate laterDate = earlierDate.plusDays(1);
        Map<LocalDate, Integer> dailyCounts = new HashMap<>(Map.of(laterDate, 7, earlierDate, 5));

        production.setId(12L);
        production.setPollaio(new Pollaio());
        production.setUovaPerGiorno(dailyCounts);

        assertEquals(12L, production.getId());
        assertNotNull(production.getPollaio());
        assertEquals(List.of(earlierDate, laterDate), production.getUovaPerGiorno().keySet().stream().toList());
        assertEquals(5, production.cercaPerGiorno(earlierDate));
        assertEquals(-1, production.cercaPerGiorno(earlierDate.minusDays(1)));

        production.aggiungiUova(laterDate, 9);
        assertEquals(9, production.cercaPerGiorno(laterDate));
    }

    @Test
    void createsProductionAndAssociatesItWithTheHenhouse() {
        Pollaio pollaio = new Pollaio();
        when(produzioneDAO.save(any(DatiProduzione.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DatiProduzione result = produzioneService.createProduzione(pollaio);

        assertSame(pollaio, result.getPollaio());
        assertSame(result, pollaio.getProduzione());
        verify(produzioneDAO).save(result);
    }

    @Test
    void updatesDailyEggCountAndSumsProductionAfterDate() {
        DatiProduzione production = new DatiProduzione();
        production.setId(4L);
        LocalDate date = LocalDate.of(2025, 2, 1);
        when(produzioneDAO.save(production)).thenReturn(production);
        when(produzioneDAO.findUovaAfterDate(4L, date)).thenReturn(List.of(3, 5, 2));

        assertSame(production, produzioneService.aggiornaUovaGiorno(6, production, date));
        assertEquals(6, production.cercaPerGiorno(date));
        assertEquals(10, produzioneService.getProduzionePeriodo(date, production));
        verify(produzioneDAO).findUovaAfterDate(4L, date);
    }
}
