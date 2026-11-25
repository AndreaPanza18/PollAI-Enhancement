
package com.example.pollai.utente;

import com.example.pollai.pollaio.Pollaio;
import com.example.pollai.utente.dto.LoginRequest;
import com.example.pollai.utente.dto.LoginResponse;
import com.example.pollai.utente.dto.RegisterRequest;
import com.example.pollai.utente.dto.TokenJWTDto;
import com.example.pollai.utente.dto.UtenteResponse;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UtenteService {

    private static final String DEFAULT_ROLE = "USER";
    private static final String TOKEN_TYPE = "Bearer";

    private static final Logger log =
            LoggerFactory.getLogger(UtenteService.class);

    private final UtenteDAO utenteDAO;
    private final TokenJWTService tokenJWTService;

    public UtenteService(
            UtenteDAO utenteDAO,
            TokenJWTService tokenJWTService
    ) {
        this.utenteDAO = utenteDAO;
        this.tokenJWTService = tokenJWTService;
    }

    // Autenticazione: restituisce la LoginResponse oppure empty se le credenziali non sono valide
    public Optional<LoginResponse> login(LoginRequest request) {
        return autenticazione(request.email(), request.password())
                .map(this::createLoginResponse);
    }

    // Registrazione: crea l'utente e restituisce la LoginResponse
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

    // Elenco degli utenti convertiti in DTO
    public List<UtenteResponse> getAllUtentiResponse() {
        return getAllUtenti().stream()
                .map(UtenteResponse::from)
                .toList();
    }

    // Restituisce il profilo dell'utente identificato dall'ID autenticato
    public UtenteResponse getProfilo(Long userId) {
        return UtenteResponse.from(getUtenteByIdOrThrow(userId));
    }

    // Restituisce l'area utente solo se il pollaio è configurato
    public UtenteResponse getAreaUtente(Long userId) {
        Utente utente = getUtenteByIdOrThrow(userId);

        if (!isPollaioConfigurato(utente)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Pollaio non configurato"
            );
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

    // Recupera un utente tramite ID oppure restituisce 404
    private Utente getUtenteByIdOrThrow(Long userId) {
        return utenteDAO.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Utente non trovato"
                ));
    }

    // Un pollaio è configurato se esiste e ha almeno una gallina
    private boolean isPollaioConfigurato(Utente utente) {
        Pollaio pollaio = utente.getPollaio();
        return pollaio != null && pollaio.getQuantity() > 0;
    }

    // Crea o aggiorna un utente salvando la password tramite BCrypt
    public Utente saveUtente(Utente utente, String password) {
        String hashedPassword = hashPassword(password);
        utente.setPassword(hashedPassword);
        return utenteDAO.save(utente);
    }

    // Ottiene tutti gli utenti
    public List<Utente> getAllUtenti() {
        return utenteDAO.findAll();
    }

    // Ottiene un utente per ID
    public Optional<Utente> getUtenteById(Long id) {
        return utenteDAO.findById(id);
    }

    // Elimina un utente per ID
    public void deleteUtente(Long id) {
        utenteDAO.deleteById(id);
    }

    // Autenticazione tramite email e password
    public Optional<Utente> autenticazione(String email, String password) {
        Optional<Utente> userCheck = utenteDAO.findByEmail(email);

        if (userCheck.isPresent()) {
            log.info("L'email è stata trovata");

            Utente user = userCheck.get();

            if (!verifyPassword(password, user.getPassword())) {
                return Optional.empty();
            }
        }

        return userCheck;
    }

    // Hash della password
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Verifica della password
    public static boolean verifyPassword(
            String plainPassword,
            String hashedPassword
    ) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
