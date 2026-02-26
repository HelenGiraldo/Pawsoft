package co.edu.uniquindio.backendpawsoft.exception;

/**
 * Excepción que se lanza cuando un recurso
 * no es encontrado en el sistema.
 *
 * Representa un error HTTP 404 (Not Found).
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
