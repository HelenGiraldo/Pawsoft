package co.edu.uniquindio.backendpawsoft.exception;

/**
 * Excepción lanzada cuando un usuario intenta acceder a un recurso sin autorización.
 * 
 * Esta excepción se utiliza cuando un usuario intenta realizar operaciones
 * sobre recursos que no le pertenecen o para los cuales no tiene permisos.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Programa: Ingeniería de Sistemas y Computación
 * Materia: Software III
 *
 * Autoras:
 * - Valentina Porras Salazar
 * - Helen Xiomara Giraldo Libreros
 *
 * Profesor:
 * Raúl Yulbraynner Rivera Gálvez
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedAccessException(String resource, Long userId) {
        super("Usuario con ID " + userId + " no tiene autorización para acceder al recurso: " + resource);
    }
}