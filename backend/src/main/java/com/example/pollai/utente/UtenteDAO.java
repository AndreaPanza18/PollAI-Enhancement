package com.example.pollai.utente;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UtenteDAO extends JpaRepository<Utente, Long> {
    Optional<Utente> findByEmail(String email);

    /**
     * Recupera un utente caricando anche la relazione {@code pollaio} con le sue {@code galline}.
     * Utilizza un {@code EntityGraph} per evitare il lazy loading e garantire che il pollaio
     * sia inizializzato anche fuori da un contesto transazionale.
     *
     * @param id l'ID dell'utente
     * @return un {@code Optional<Utente>} con il pollaio inizializzato
     */
    @EntityGraph(attributePaths = {"pollaio.galline"})
    @Query("SELECT u FROM Utente u WHERE u.id = :id")
    Optional<Utente> findByIdWithPollaio(Long id);
}