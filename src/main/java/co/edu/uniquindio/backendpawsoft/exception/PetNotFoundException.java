package co.edu.uniquindio.backendpawsoft.exception;

/**
 * Excepción lanzada cuando no se encuentra una mascota específica.
 * 
 * Esta excepción se utiliza cuando se intenta acceder a una mascota
 * que no existe en la base de datos o al cual el usuario no tiene acceso.
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
public class PetNotFoundException extends RuntimeException {

    public PetNotFoundException(String message) {
        super(message);
    }

    public PetNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PetNotFoundException(Long petId) {
        super("Mascota no encontrada con ID: " + petId);
    }
}