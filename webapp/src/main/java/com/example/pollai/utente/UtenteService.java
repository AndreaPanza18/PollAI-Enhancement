package com.example.pollai.utente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
//@ nullable_by_default
public class UtenteService {

    // Inietta il repository per l'interazione con il database
    // spec_public: visibile nelle specifiche; nullable: lo valorizza Spring, non il costruttore
    @Autowired
    private /*@ spec_public nullable @*/ UtenteDAO utenteDAO;

    // Crea un logger per il controllo dei log
    private static final Logger log = LoggerFactory.getLogger(UtenteService.class);

    // Crea o aggiorna un Utente (salva)
    /*@ public behavior
    @     requires utente != null && password != null && utenteDAO != null;
    @     assignable \everything;
    @     ensures \result != null && \result.password != null
    @          && verifyPassword(password, \result.password);
    @     signals_only RuntimeException;
    @*/
    public Utente saveUtente(Utente utente, String password) {
        // Hash della password prima di salvarla
        String hashedPassword = hashPassword(password);
        // Imposta la password criptata sull'utente
        utente.setPassword(hashedPassword);
        // Salva l'utente nel repository e restituisce l'utente salvato
        Utente saved = utenteDAO.save(utente);
        //@ assume saved != null && saved.password != null && verifyPassword(password, saved.password);
        return saved;
    }

    // Ottiene tutti gli utenti
    /*@ public behavior
      @     requires utenteDAO != null;
      @     assignable \everything;
      @     ensures \result != null;
      @     signals_only RuntimeException;
      @*/
    public List<Utente> getAllUtenti() {
        // Restituisce una lista di tutti gli utenti
        List<Utente> utenti = utenteDAO.findAll();
        //@ assume utenti != null;
        return utenti;
    }

    // Ottiene un Utente per ID
    /*@ public behavior
      @     requires id != null && utenteDAO != null;
      @     assignable \everything;
      @     ensures \result != null;
      @     signals_only RuntimeException;
      @*/
    public Optional<Utente> getUtenteById(Long id) {
        // Restituisce un utente opzionale trovato tramite ID
        Optional<Utente> utente = utenteDAO.findById(id);
        //@ assume utente != null;
        return utente;
    }

    // Elimina un Utente per ID
    /*@ public behavior
      @     requires id != null && utenteDAO != null;
      @     assignable \everything;
      @     signals_only RuntimeException;
      @*/
    public void deleteUtente(Long id) {
        // Elimina l'utente con il dato ID
        utenteDAO.deleteById(id);
    }

    // Esempio di query personalizzata: Ottieni un Utente tramite email
    /*@ public behavior
      @     requires email != null && password != null && utenteDAO != null;
      @     assignable \everything;
      @     ensures \result != null;
      @     signals_only RuntimeException;
      @*/
    public Optional<Utente> autenticazione(String email, String password) {
        // Cerca un utente tramite email
        Optional<Utente> userCheck = utenteDAO.findByEmail(email);
        //@ assume userCheck != null;
        if(userCheck.isPresent()){
            log.info("L'email è stata trovata");
            Utente user = userCheck.get();
            //@ assume user != null && user.password != null;
            // Verifica se la password corrisponde
            if(!verifyPassword(password, user.getPassword())){
                // Se la password non è corretta, restituisce un risultato vuoto
                return Optional.empty();
            }
        }
        // Restituisce l'utente trovato tramite email
        return userCheck;
    }

    // Metodo per fare l'hash della password
    /*@ public behavior
      @     requires plainPassword != null;
      @     ensures \result != null;
      @     signals_only RuntimeException;
      @*/
    public /*@ skipesc @*/ static String hashPassword(String plainPassword) {
        // Restituisce la password criptata usando BCrypt
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Metodo per verificare se la password inserita corrisponde alla password criptata
    /*@ public behavior
      @     requires plainPassword != null && hashedPassword != null;
      @     ensures \result == true || \result == false;
      @     signals_only RuntimeException;
      @*/
    public /*@ pure skipesc @*/ static boolean verifyPassword(String plainPassword, String hashedPassword) {
        // Confronta la password in chiaro con quella criptata
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}