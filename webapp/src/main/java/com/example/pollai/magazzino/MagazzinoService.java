package com.example.pollai.magazzino;

import com.example.pollai.utente.Utente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
//@ nullable_by_default
public class MagazzinoService {
    @Autowired
    private /*@ spec_public nullable @*/ MagazzinoDAO magazzinoDAO;
    @Autowired
    private /*@ spec_public nullable @*/ FarmacoDAO farmacoDAO;
    @Autowired
    private /*@ spec_public nullable @*/ CiboDAO ciboDAO;


    /*@ public behavior
      @     requires utente != null && magazzinoDAO != null;
      @     assignable \everything;
      @     ensures \result != null;
      @     signals_only RuntimeException;
      @*/
    public Magazzino createMagazzino(Utente utente){
        Magazzino m = new Magazzino();
        m.setUtente(utente);
        utente.setMagazzino(m);
        Magazzino saved = magazzinoDAO.save(m);
        //@ assume saved != null;
        return saved;
    }

    /*@ public behavior
      @     requires m != null && f != null && magazzinoDAO != null;
      @     assignable \everything;
      @     ensures \result != null;
      @     signals_only RuntimeException;
      @*/
    public Magazzino addFarmaco(Magazzino m, Farmaco f){

        // Salva il farmaco per generare l'ID
        m.addFarmaco(f);

        // Salva il magazzino aggiornato
        Magazzino saved = magazzinoDAO.save(m);
        //@ assume saved != null;
        return saved;
    }

    /*@ public behavior
      @     requires m != null && farmacoId != null && farmacoDAO != null && magazzinoDAO != null;
      @     assignable \everything;
      @     ensures \result != null;
      @     signals_only RuntimeException;
      @*/
    public Magazzino removeFarmaco(Magazzino m, Long farmacoId){
        Optional<Farmaco> trovato = farmacoDAO.findById(farmacoId);
        //@ assume trovato != null;
        if (!trovato.isPresent()) {
            throw new RuntimeException("Farmaco non trovato");
        }
        Farmaco f = trovato.get();
        //@ assume f != null;
        try {
            // Rimuove il farmaco dal magazzino
            m.removeFarmaco(f);

            // Elimina il farmaco dal database
            farmacoDAO.delete(f);

            // Salva il magazzino aggiornato e lo restituisce
            Magazzino saved = magazzinoDAO.save(m);
            //@ assume saved != null;
            return saved;
        } catch (Exception e) {
            // Gestisce eventuali errori di rimozione o salvataggio
            throw new RuntimeException("Errore durante la rimozione del farmaco", e);
        }
    }

    /*@ public behavior
      @     requires m != null && c != null && magazzinoDAO != null;
      @     assignable \everything;
      @     ensures \result != null;
      @     signals_only RuntimeException;
      @*/
    public Magazzino addCibo(Magazzino m, Cibo c){
        // Aggiungi il cibo al magazzino
        m.addCibo(c);

        // Salva il magazzino aggiornato
        Magazzino saved = magazzinoDAO.save(m);
        //@ assume saved != null;
        return saved;
    }

    /*@ public behavior
      @     requires m != null && ciboId != null && ciboDAO != null && magazzinoDAO != null;
      @     assignable \everything;
      @     ensures \result != null;
      @     signals_only RuntimeException;
      @*/
    public Magazzino removeCibo(Magazzino m, Long ciboId) {
        Optional<Cibo> trovato = ciboDAO.findById(ciboId);
        //@ assume trovato != null;
        if (!trovato.isPresent()) {
            throw new NoSuchElementException("No value present");
        }
        Cibo c = trovato.get();
        //@ assume c != null;
        try {
            // Rimuove il farmaco dal magazzino
            m.removeCibo(c);

            // Elimina il farmaco dal database
            ciboDAO.delete(c);

            // Salva il magazzino aggiornato e lo restituisce
            Magazzino saved = magazzinoDAO.save(m);
            //@ assume saved != null;
            return saved;
        } catch (Exception e) {
            // Gestisce eventuali errori di rimozione o salvataggio
            throw new RuntimeException("Errore durante la rimozione del cibo", e);
        }
    }

    /*@ public behavior
      @     requires m != null && itemId != null && farmacoDAO != null && magazzinoDAO != null;
      @     assignable \everything;
      @     ensures \result != null;
      @     signals_only RuntimeException;
      @*/
    public Magazzino modificaFarmaco(Magazzino m, Long itemId, int cambio) {
        // Recupera l'oggetto Farmaco dal database utilizzando l'ID dell'item
        Optional<Farmaco> trovato = farmacoDAO.findById(itemId);
        //@ assume trovato != null;
        if (!trovato.isPresent()) {
            throw new NoSuchElementException("No value present");
        }
        Farmaco f = trovato.get();
        //@ assume f != null;
        try {
            // Calcola la nuova quantità di farmaco dopo l'operazione di aggiunta o rimozione
            long quantita = (long) f.getQuantita() + cambio;

            // Verifica se la quantità risulta negativa o zero
            if (quantita <= 0) {
                // Se la quantità è 0 o negativa, non è possibile modificare il farmaco (lo lascia invariato)
                return m;
            } else {
                if (quantita > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("Quantità fuori range");
                }
                // Altrimenti, aggiorna la quantità del farmaco con il nuovo valore calcolato
                f.setQuantita((int) quantita);
                // Salva l'oggetto Farmaco aggiornato nel database
                Farmaco farmacoSalvato = farmacoDAO.save(f);
                //@ assume farmacoSalvato != null;
            }

            // Salva il Magazzino aggiornato nel database e lo restituisce
            Magazzino saved = magazzinoDAO.save(m);
            //@ assume saved != null;
            return saved;
        } catch (Exception e) {
            // Gestisce eventuali errori che potrebbero verificarsi durante la modifica del farmaco
            // (ad esempio problemi con il salvataggio o la rimozione)
            throw new RuntimeException("Errore durante la modifica del farmaco", e);
        }
    }

    /*@ public behavior
      @     requires m != null && itemId != null && ciboDAO != null && magazzinoDAO != null;
      @     assignable \everything;
      @     ensures \result != null;
      @     signals_only RuntimeException;
      @*/
    public Magazzino modificaCibo(Magazzino m, Long itemId, int cambio) {
        // Recupera l'oggetto Cibo dal database utilizzando l'ID dell'item
        Optional<Cibo> trovato = ciboDAO.findById(itemId);
        //@ assume trovato != null;
        if (!trovato.isPresent()) {
            throw new NoSuchElementException("No value present");
        }
        Cibo c = trovato.get();
        //@ assume c != null;
        try {
            // Calcola la nuova quantità di cibo dopo l'operazione di aggiunta o rimozione
            long quantita = (long) c.getQuantita() + cambio;

            // Verifica se la quantità risulta negativa o zero
            if (quantita <= 0) {
                // Se la quantità è 0 o negativa, non è possibile modificare il cibo (lo lascia invariato)
                return m;
            } else {
                if (quantita > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("Quantità fuori range");
                }
                // Altrimenti, aggiorna la quantità del cibo con il nuovo valore calcolato
                c.setQuantita((int) quantita);
                // Salva l'oggetto Cibo aggiornato nel database
                Cibo ciboSalvato = ciboDAO.save(c);
                //@ assume ciboSalvato != null;
            }

            // Salva il Magazzino aggiornato nel database e lo restituisce
            Magazzino saved = magazzinoDAO.save(m);
            //@ assume saved != null;
            return saved;
        } catch (Exception e) {
            // Gestisce eventuali errori che potrebbero verificarsi durante la modifica del cibo
            // (ad esempio problemi con il salvataggio o la rimozione)
            throw new RuntimeException("Errore durante la modifica del cibo", e);
        }
    }
}
