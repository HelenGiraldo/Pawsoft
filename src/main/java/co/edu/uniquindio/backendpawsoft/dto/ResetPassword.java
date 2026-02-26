package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Data;

/**
 * DTO para finalizar el proceso de restablecimiento de contraseña.
 *
 * Se envía cuando el usuario ya recibió un token de recuperación (por correo)
 * y desea establecer una nueva contraseña.
 *
 * Contiene:
 * - token: token válido y no expirado generado por el sistema.
 * - newPassword: nueva contraseña que debe cumplir las reglas de seguridad definidas.
 *
 * Nota: no se debe registrar (loggear) el token ni la contraseña.
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
public class ResetPassword {

    /**
     * Token de restablecimiento entregado al usuario (por ejemplo, vía correo).
     * Debe existir, no estar expirado y no haber sido usado.
     */
    private String token;

    /**
     * Nueva contraseña que el usuario desea establecer.
     * Debe cumplir las reglas de fortaleza definidas por el backend.
     */
    private String newPassword;
}