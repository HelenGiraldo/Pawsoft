package co.edu.uniquindio.backendpawsoft.exception;

/**
 * Excepción lanzada cuando un usuario intenta acceder a una mascota sin autorización.
 * 
 * Esta excepción se utiliza cuando un usuario intenta realizar operaciones
 * sobre mascotas que no le pertenecen o para las cuales no tiene permisos.
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
public class UnauthorizedPetAccessException extends RuntimeException {

    public UnauthorizedPetAccessException(String message) {
        super(message);
    }

    public UnauthorizedPetAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedPetAccessException(Long petId, Long userId) {
        super("Usuario con ID " + userId + " no tiene autorización para acceder a la mascota con ID: " + petId);
    }
}