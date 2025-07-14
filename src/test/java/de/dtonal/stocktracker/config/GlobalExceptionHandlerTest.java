package de.dtonal.stocktracker.config;

import de.dtonal.stocktracker.model.PortfolioNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleResponseStatusException() {
        // Given
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot");

        // When
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleResponseStatusException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
        assertThat(response.getBody()).containsEntry("error", "I'm a teapot");
    }

    @Test
    void handleBadCredentialsException() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

        // When
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleBadCredentialsException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "Invalid credentials");
    }

    @Test
    void handleAccessDeniedException() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Forbidden");

        // When
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAccessDeniedException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).containsEntry("error", "Forbidden");
    }

    @Test
    void handlePortfolioNotFoundException() {
        // Given
        PortfolioNotFoundException exception = new PortfolioNotFoundException("Portfolio not here");

        // When
        ResponseEntity<Map<String, String>> response = exceptionHandler.handlePortfolioNotFoundException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Portfolio not here");
    }

    @Test
    void handleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Bad argument");

        // When
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Bad argument");
    }

    @Test
    void handleGenericException() {
        // Given
        Exception exception = new Exception("Something broke");

        // When
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("error", "An unexpected error occurred");
    }
} 