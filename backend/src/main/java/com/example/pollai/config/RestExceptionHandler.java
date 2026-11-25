package com.example.pollai.config;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Converte le eccezioni dei @RestController (ResponseStatusException, validazione, ecc.)
 * in risposte JSON standard RFC 7807 (ProblemDetail). I controller Thymeleaf restano esclusi.
 */
@RestControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
}