
package com.example.pollai.utente;

import com.example.pollai.utente.dto.LoginRequest;
import com.example.pollai.utente.dto.RegisterRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UtenteRequestValidationTest {

    private ValidatorFactory factory;
    private Validator validator;

    @BeforeEach
    void setup() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    @Test
    void loginRequestAcceptsValidData() {
        LoginRequest request = new LoginRequest(
                "mario.rossi@example.com",
                "Sup3rm@n!"
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void loginRequestRejectsBlankEmail() {
        LoginRequest request = new LoginRequest(
                "",
                "Sup3rm@n!"
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void loginRequestRejectsInvalidEmail() {
        LoginRequest request = new LoginRequest(
                "email-non-valida",
                "Sup3rm@n!"
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void registerRequestAcceptsValidData() {
        RegisterRequest request = new RegisterRequest(
                "Mario",
                "Rossi",
                "3331234567",
                true,
                "IT12345678901",
                "mario.rossi@example.com",
                "Sup3rm@n!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void registerRequestRejectsBlankRequiredFields() {
        RegisterRequest request = new RegisterRequest(
                "",
                "",
                "",
                true,
                null,
                "",
                ""
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("nome")));

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("cognome")));

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")));

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void registerRequestRejectsInvalidEmail() {
        RegisterRequest request = new RegisterRequest(
                "Mario",
                "Rossi",
                "3331234567",
                true,
                null,
                "email-non-valida",
                "Sup3rm@n!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void registerRequestRejectsPasswordShorterThanEightCharacters() {
        RegisterRequest request = new RegisterRequest(
                "Mario",
                "Rossi",
                "3331234567",
                true,
                null,
                "mario.rossi@example.com",
                "short"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void registerRequestRejectsFieldsExceedingDatabaseLengths() {
        RegisterRequest request = new RegisterRequest(
                "NomeTroppoLungoPerIlDatabase",
                "Rossi",
                "3331234567",
                true,
                null,
                "mario.rossi@example.com",
                "Sup3rm@n!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("nome")));
    }
}
