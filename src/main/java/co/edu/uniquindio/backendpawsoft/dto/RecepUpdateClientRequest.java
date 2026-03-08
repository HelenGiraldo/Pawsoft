package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para actualizar datos básicos de un cliente desde el panel de recepcionista.
 *
 * El email y el teléfono son opcionales — solo se actualizan si vienen con valor.
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
@Data
public class RecepUpdateClientRequest {

    /** Nombre completo del cliente — obligatorio. */
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    /**
     * Nuevo correo electrónico del cliente — opcional.
     * Si se envía, debe ser un email válido y no estar en uso por otro usuario.
     */
    @Email(message = "El correo no es válido")
    private String email;

    /** Teléfono de contacto — opcional. */
    private String telefono;
}