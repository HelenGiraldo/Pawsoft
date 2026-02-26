package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitar el cambio de contraseña durante el primer inicio de sesión.
 *
 * Se utiliza cuando un usuario ingresa con una contraseña temporal y debe establecer
 * una nueva contraseña para continuar usando el sistema.
 *
 * Las validaciones se ejecutan automáticamente cuando este DTO se usa junto con
 * la anotación {@code @Valid} en el controlador.
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
public class ChangePasswordRequest {

    /**
     * Correo electrónico del usuario que realizará el cambio de contraseña.
     */
    @NotBlank(message = "El correo es obligatorio")
    private String email;

    /**
     * Nueva contraseña que el usuario desea establecer.
     */
    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String newPassword;

    /**
     * Retorna el correo electrónico del usuario.
     *
     * @return email del usuario
     */
    public String getEmail() {
        return email;
    }

    /**
     * Asigna el correo electrónico del usuario.
     *
     * @param email correo del usuario
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retorna la nueva contraseña solicitada.
     *
     * @return nueva contraseña
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * Asigna la nueva contraseña solicitada.
     *
     * @param newPassword nueva contraseña
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}