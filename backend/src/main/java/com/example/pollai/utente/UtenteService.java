package com.example.pollai.utente;

import com.example.pollai.pollaio.Pollaio;
import com.example.pollai.utente.dto.LoginRequest;
import com.example.pollai.utente.dto.LoginResponse;
import com.example.pollai.utente.dto.RegisterRequest;
import com.example.pollai.utente.dto.UtenteResponse;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.pollai.utente.dto.TokenJWTDto;

import java.util.List;
import java.util.Optional;

@Service
public class UtenteService {

    private static final String DEFAULT_ROLE = "USER";
    private static final String TOKEN_TYPE = "Bearer";

    // Inietta il repository per l'interazione con il database
    @Autowired
    private UtenteDAO utenteDAO;

    @Autowired
    private TokenJWTService tokenJWTService;

    // Crea un logger per il controllo dei log
    private static final Logger log = LoggerFactory.getLogger(UtenteService.class);

    // Autenticazione: restituisce la LoginResponse oppure empty se le credenziali non sono valide
    public Optional<LoginResponse> login(LoginRequest request) {
        return autenticazione(request.email(), request.password())
                .map(this::createLoginResponse);
    }

    // Registrazione: spacchetta la RegisterRequest, salva l'utente e restituisce la LoginResponse
    public LoginResponse register(RegisterRequest request) {
        Utente utente = new Utente();
        utente.setNome(request.nome());
        utente.setCognome(request.cognome());
        utente.setPhone_number(request.phoneNumber());
        utente.setRasvsl(request.rasvsl());
        utente.setPIVA(request.pIVA());
        utente.setEmail(request.email());

        Utente saved = saveUtente(utente, request.password());
        return createLoginResponse(saved);
    }

    // Elenco di tutti gli utenti come DTO pubblici
    public List<UtenteResponse> getAllUtentiResponse() {
        return getAllUtenti().stream()
                .map(UtenteResponse::from)
                .toList();
    }

    // Profilo dell'utente identificato dal token.
    // JwtException / IllegalArgumentException (token non valido) vengono propagate al controller.
    public UtenteResponse getProfilo(String token) {
        return UtenteResponse.from(getUtenteFromToken(token));
    }

    // Area utente: come il profilo, ma solo se il pollaio è configurato (altrimenti 409)
    public UtenteResponse getAreaUtente(String token) {
        Utente utente = getUtenteFromToken(token);
        if (!isPollaioConfigurato(utente)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pollaio non configurato");
        }
        return UtenteResponse.from(utente);
    }

    // Costruisce la LoginResponse (token + flag pollaio) per l'utente
    private LoginResponse createLoginResponse(Utente utente) {
        TokenJWTDto token = tokenJWTService.generateToken(
                utente.getId().toString(),
                utente.getEmail(),
                DEFAULT_ROLE
        );
        return new LoginResponse(
                token.token(),
                TOKEN_TYPE,
                tokenJWTService.getJwtExpirationSeconds(),
                UtenteResponse.from(utente),
                isPollaioConfigurato(utente)
        );
    }

    // Recupera l'utente a partire dall'ID contenuto nel token (letto tramite TokenJWTService)
    private Utente getUtenteFromToken(String token) {
        Long userId = Long.valueOf(tokenJWTService.extractUserId(token));
        return getUtenteById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));
    }

    // Un pollaio è configurato se esiste e ha almeno una gallina
    private boolean isPollaioConfigurato(Utente utente) {
        Pollaio pollaio = utente.getPollaio();
        return pollaio != null && pollaio.getQuantity() > 0;
    }

    // Crea o aggiorna un Utente (salva)
    public Utente saveUtente(Utente utente, String password) {
        // Hash della password prima di salvarla
        String hashedPassword = hashPassword(password);
        // Imposta la password criptata sull'utente
        utente.setPassword(hashedPassword);
        // Salva l'utente nel repository e restituisce l'utente salvato
        return utenteDAO.save(utente);
    }

   // Ottiene tutti gli utenti
    public List<Utente> getAllUtenti() {
        // Restituisce una lista di tutti gli utenti
        return utenteDAO.findAll();
    }

    // Ottiene un Utente per ID
    public Optional<Utente> getUtenteById(Long id) {
        // Restituisce un utente opzionale trovato tramite ID
        return utenteDAO.findById(id);
    }

    // Elimina un Utente per ID
    public void deleteUtente(Long id) {
        // Elimina l'utente con il dato ID
        utenteDAO.deleteById(id);
    }

    // Esempio di query personalizzata: Ottieni un Utente tramite email
    public Optional<Utente> autenticazione(String email, String password) {
        // Cerca un utente tramite email
        Optional<Utente> userCheck = utenteDAO.findByEmail(email);
        if(userCheck.isPresent()){
            log.info("L'email è stata trovata");
            Utente user = userCheck.get();
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
    public static String hashPassword(String plainPassword) {
        // Restituisce la password criptata usando BCrypt
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Metodo per verificare se la password inserita corrisponde alla password criptata
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        // Confronta la password in chiaro con quella criptata
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}