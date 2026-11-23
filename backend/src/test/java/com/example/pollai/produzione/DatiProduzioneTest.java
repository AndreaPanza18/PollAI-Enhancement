package com.example.pollai.produzione;

import com.example.pollai.pollaio.Pollaio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DatiProduzioneTest {

    @Test
    void storesIdAndPollaio() {
        DatiProduzione produzione = new DatiProduzione();
        Pollaio pollaio = new Pollaio();
        produzione.setId(7L);
        produzione.setPollaio(pollaio);

        assertEquals(7L, produzione.getId());
        assertSame(pollaio, produzione.getPollaio());
    }

    @Test
    void addsAndLooksUpEggCountsByDate() {
        DatiProduzione produzione = new DatiProduzione();
        LocalDate today = LocalDate.of(2026, 11, 22);
        LocalDate yesterday = today.minusDays(1);

        produzione.aggiungiUova(today, 12);

        assertEquals(12, produzione.cercaPerGiorno(today));
        assertEquals(-1, produzione.cercaPerGiorno(yesterday));
        assertEquals(Map.of(today, 12), produzione.getUovaPerGiorno());
    }

    @Test
    void returnsEggCountsSortedByDateAndAllowsReplacingMap() {
        DatiProduzione produzione = new DatiProduzione();
        LocalDate later = LocalDate.of(2026, 11, 22);
        LocalDate earlier = later.minusDays(1);
        produzione.setUovaPerGiorno(Map.of(later, 2, earlier, 1));

        assertEquals(earlier, produzione.getUovaPerGiorno().keySet().iterator().next());
        assertEquals(2, produzione.cercaPerGiorno(later));
    }
}
