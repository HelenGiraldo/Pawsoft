package co.edu.uniquindio.backendpawsoft.dto;

import co.edu.uniquindio.backendpawsoft.enums.Role;
import lombok.Data;

/**
 * DTO que representa la información necesaria para crear un usuario de tipo staff.
 *
 * Se usa como cuerpo de la solicitud en el endpoint de creación de personal
 * (por ejemplo, veterinario o recepcionista).
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
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
public class StaffUserRequest {

    /**
     * Nombre completo del usuario staff.
     */
    private String nombre;

    /**
     * Correo electrónico del usuario staff.
     */
    private String email;

    /**
     * Rol asignado al usuario staff.
     * Debe corresponder a un rol permitido para personal (por ejemplo: veterinario o recepcionista).
     */
    private Role role; // VETERINARIO o RECEPCIONISTA


    /** URL de foto subida a Cloudinary — opcional, solo aplica para veterinarios */
    private String photoUrl;
}