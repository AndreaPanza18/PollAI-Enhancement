package com.example.pollai.utente;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import com.example.pollai.exceptions.TokenJWTException;
import com.example.pollai.utente.dto.TokenJWTDto;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TokenJWTServiceTest {

    private TokenJWTService service;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();

        service = new TokenJWTService();
        setField("privateKeyString", Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        setField("publicKeyString", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        setField("jwtExpiration", 3600L);
    }

    @Test
    void generateTokenAndExtractsSubjectUsernameAndRole() {
        TokenJWTDto dto = service.generateToken("42", "mario@example.com", "ADMIN");

        assertNotNull(dto.token());
        assertEquals("42", service.extractUserId(dto.token()));
        assertEquals("mario@example.com", service.extractUsername(dto.token()));
        assertEquals("ADMIN", service.extractRole(dto.token()));
        assertTrue(service.isTokenValid(dto.token()));
    }

    @Test
    void generateTokenRejectsNullClaims() {
        assertAll(
                () -> assertThrows(TokenJWTException.class,
                        () -> service.generateToken(null, "mario@example.com", "USER")),
                () -> assertThrows(TokenJWTException.class,
                        () -> service.generateToken("1", null, "USER")),
                () -> assertThrows(TokenJWTException.class,
                        () -> service.generateToken("1", "mario@example.com", null))
        );
    }

    @Test
    void expiredTokenIsNotValid() {
        String expiredToken = Jwts.builder()
                .setSubject("42")
                .claim("username", "mario@example.com")
                .claim("role", "USER")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10_000))
                .setExpiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        assertFalse(service.isTokenValid(expiredToken));
    }

    @Test
    void invalidOrTamperedTokenIsRejected() {
        assertThrows(JwtException.class, () -> service.extractUserId("not-a-jwt"));
        String otherKeyToken = Jwts.builder()
                .setSubject("42")
                .signWith(newKeyPair().getPrivate(), SignatureAlgorithm.RS256)
                .compact();
        assertThrows(JwtException.class, () -> service.extractUserId(otherKeyToken));
    }

    @Test
    void refreshTokenPreservesIdentityAndClaimsAndIssuesValidToken() {
        String original = service.generateToken("42", "mario@example.com", "USER").token();

        TokenJWTDto refreshed = service.refreshToken(original);

        assertNotNull(refreshed.token());
        assertEquals("42", service.extractUserId(refreshed.token()));
        assertEquals("mario@example.com", service.extractUsername(refreshed.token()));
        assertEquals("USER", service.extractRole(refreshed.token()));
        assertTrue(service.isTokenValid(refreshed.token()));
    }

    @Test
    void parseTokenWrapsTokenWithoutChangingIt() {
        assertEquals("opaque-token", service.parseToken("opaque-token").token());
    }

    @Test
    void returnsConfiguredExpirationInSeconds() {
        assertEquals(3600L, service.getJwtExpirationSeconds());
    }

    private void setField(String name, Object value) throws Exception {
        Field field = TokenJWTService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(service, value);
    }

    private KeyPair newKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // ===== Casi aggiuntivi per la copertura =====

    private static final String NOT_BASE64 = "%%%non-base64%%%";
    private static final String BASE64_BUT_NOT_A_KEY = Base64.getEncoder().encodeToString("abc".getBytes());

    @Test
    void privateKeyNotInBase64ThrowsRuntimeException() throws Exception {
        setField("privateKeyString", NOT_BASE64);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.generateToken("1", "mario@example.com", "USER"));

        assertEquals("Chiave privata non è in formato Base64 valido", ex.getMessage());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void privateKeyWithInvalidSpecThrowsRuntimeException() throws Exception {
        setField("privateKeyString", BASE64_BUT_NOT_A_KEY);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.generateToken("1", "mario@example.com", "USER"));

        assertEquals("Errore prv_key, controlla application.properties", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    void publicKeyNotInBase64ThrowsRuntimeException() throws Exception {
        setField("publicKeyString", NOT_BASE64);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.extractUserId("a.b.c"));

        assertEquals("Chiave pubblica non è in formato Base64 valido", ex.getMessage());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void publicKeyWithInvalidSpecThrowsRuntimeException() throws Exception {
        setField("publicKeyString", BASE64_BUT_NOT_A_KEY);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.extractUserId("a.b.c"));

        assertEquals("Errore pub_key, controlla application.properties", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    void keysAreDecodedOnlyOnceAndReused() throws Exception {
        assertNull(getField("privateKey"));
        assertNull(getField("publicKey"));

        String token = service.generateToken("1", "mario@example.com", "USER").token();
        service.extractUserId(token);
        Object privateAfterFirstCall = getField("privateKey");
        Object publicAfterFirstCall = getField("publicKey");
        assertNotNull(privateAfterFirstCall);
        assertNotNull(publicAfterFirstCall);

        // seconda chiamata: ramo "chiave già in cache"
        service.generateToken("2", "luigi@example.com", "USER");
        service.extractUserId(token);

        assertSame(privateAfterFirstCall, getField("privateKey"));
        assertSame(publicAfterFirstCall, getField("publicKey"));
    }

    @Test
    void generatedTokenExpiresAfterConfiguredSeconds() {
        String token = service.generateToken("42", "mario@example.com", "USER").token();

        Date issuedAt = service.extractClaim(token, Claims::getIssuedAt);
        Date expiration = service.extractClaim(token, Claims::getExpiration);

        assertEquals(3_600_000L, expiration.getTime() - issuedAt.getTime());
    }

    @Test
    void missingUsernameAndRoleClaimsAreReturnedAsNull() {
        String token = Jwts.builder()
                .setSubject("42")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        assertEquals("42", service.extractUserId(token));
        assertNull(service.extractUsername(token));
        assertNull(service.extractRole(token));
    }

    @Test
    void isTokenValidPropagatesSignatureErrors() {
        String foreignToken = Jwts.builder()
                .setSubject("42")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(newKeyPair().getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        assertThrows(JwtException.class, () -> service.isTokenValid(foreignToken));
    }

    @Test
    void isTokenValidPropagatesMalformedTokenErrors() {
        assertThrows(JwtException.class, () -> service.isTokenValid("not-a-jwt"));
    }

    @Test
    void refreshTokenRejectsExpiredToken() {
        String expired = Jwts.builder()
                .setSubject("42")
                .claim("username", "mario@example.com")
                .claim("role", "USER")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10_000))
                .setExpiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> service.refreshToken(expired));
    }

    @Test
    void refreshTokenRejectsInvalidToken() {
        assertThrows(JwtException.class, () -> service.refreshToken("not-a-jwt"));
    }

    @Test
    void refreshTokenFailsWhenClaimsAreMissing() {
        String incomplete = Jwts.builder()
                .setSubject("42")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        assertThrows(TokenJWTException.class, () -> service.refreshToken(incomplete));
    }

    private Object getField(String name) throws Exception {
        Field field = TokenJWTService.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(service);
    }
}