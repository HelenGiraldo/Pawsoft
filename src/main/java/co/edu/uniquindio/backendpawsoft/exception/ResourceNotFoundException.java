package co.edu.uniquindio.backendpawsoft.exception;

/**
 * Excepción lanzada cuando un recurso solicitado no existe en el sistema.
 * Representa un error HTTP 404 (Not Found).
 * Manejada globalmente por {@link ApiExceptionHandler}.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}