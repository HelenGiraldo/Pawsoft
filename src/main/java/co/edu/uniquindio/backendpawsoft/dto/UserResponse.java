package co.edu.uniquindio.backendpawsoft.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO de respuesta que representa la información pública de un usuario.
 *
 * Se utiliza para retornar datos de usuarios en los endpoints del sistema,
 * evitando exponer información sensible como la contraseña.
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
@AllArgsConstructor
public class UserResponse {

    /**
     * Identificador único del usuario.
     */
    private Long id;

    /**
     * Nombre completo del usuario.
     */
    private String name;

    /**
     * Correo electrónico del usuario.
     */
    private String email;

    /**
     * Rol del usuario dentro del sistema (representado como texto).
     * Usualmente corresponde al nombre del enum Role (por ejemplo: ROLE_ADMIN, ROLE_CLIENTE).
     */
    private String role;
}