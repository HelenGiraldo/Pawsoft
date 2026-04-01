package co.edu.uniquindio.backendpawsoft.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO encargado de representar la respuesta enviada
 * por el sistema tras un proceso de autenticación exitoso.
 *
 * Esta clase encapsula la información devuelta al cliente
 * cuando las credenciales son válidas.
 *
 * Incluye:
 * - Mensaje informativo
 * - Email del usuario autenticado
 * - Rol asignado
 * - Token JWT
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
public class LoginResponse {

    /**
     * Mensaje que indica el resultado del proceso de autenticación.
     */
    private String message;

    /**
     * Correo electrónico del usuario autenticado.
     */
    private String email;

    
    /**
     * Rol asignado al usuario (ADMIN, VETERINARIO, etc.).
     */
    private String role;

    /**
     * Token JWT generado tras autenticación exitosa.
     */
    private String token;

    /**
     * Refresh token para renovar el access token sin re-autenticación.
     */
    private String refreshToken;

    private boolean mustChangePassword;
}