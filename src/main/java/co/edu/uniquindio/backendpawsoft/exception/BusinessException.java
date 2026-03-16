package co.edu.uniquindio.backendpawsoft.exception;

/**
 * Excepción lanzada cuando se viola una regla de negocio de la aplicación.
 * Representa un error HTTP 400 (Bad Request).
 * Manejada globalmente por {@link ApiExceptionHandler}.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}