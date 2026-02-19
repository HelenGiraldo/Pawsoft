package co.edu.uniquindio.backendpawsoft.exception;

/**
 * Excepción que se lanza cuando las credenciales
 * de autenticación son inválidas.
 *
 * Representa un error HTTP 401 (Unauthorized).
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
