package co.edu.uniquindio.backendpawsoft.exception;

/**
 * Excepción lanzada cuando no se encuentra un registro médico específico.
 * 
 * Esta excepción se utiliza cuando se intenta acceder a un registro médico
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
public class MedicalRecordNotFoundException extends RuntimeException {

    public MedicalRecordNotFoundException(String message) {
        super(message);
    }

    public MedicalRecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MedicalRecordNotFoundException(Long recordId) {
        super("Registro médico no encontrado con ID: " + recordId);
    }
}