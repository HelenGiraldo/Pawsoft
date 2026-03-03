package co.edu.uniquindio.backendpawsoft.exception;

import co.edu.uniquindio.backendpawsoft.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el manejador global de excepciones.
 * 
 * Valida que las excepciones personalizadas se manejen correctamente
 * y retornen las respuestas HTTP apropiadas con mensajes descriptivos.
 */
@DisplayName("Pruebas del Manejador Global de Excepciones")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("NotFoundException debe retornar 404 NOT_FOUND")
    void testHandleNotFoundException() {
        // Arrange
        NotFoundException exception = new NotFoundException("Recurso no encontrado");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFound(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Recurso no encontrado", response.getBody().getMessage());
    }

    @Test
    @DisplayName("UnauthorizedException debe retornar 401 UNAUTHORIZED")
    void testHandleUnauthorizedException() {
        // Arrange
        UnauthorizedException exception = new UnauthorizedException("Acceso no autorizado");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnauthorized(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Acceso no autorizado", response.getBody().getMessage());
    }

    @Test
    @DisplayName("BusinessException debe retornar 400 BAD_REQUEST")
    void testHandleBusinessException() {
        // Arrange
        BusinessException exception = new BusinessException("Error de negocio");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRuntimeException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error de negocio", response.getBody().getMessage());
    }

    @Test
    @DisplayName("RuntimeException genérica debe retornar 400 BAD_REQUEST")
    void testHandleRuntimeException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Error en tiempo de ejecución");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRuntimeException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error en tiempo de ejecución", response.getBody().getMessage());
    }

    @Test
    @DisplayName("ErrorResponse debe contener timestamp")
    void testErrorResponseContieneTimestamp() {
        // Arrange
        NotFoundException exception = new NotFoundException("Test");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFound(exception);

        // Assert
        assertNotNull(response.getBody().getTimestamp());
    }
}
