package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO para la creación o actualización de un usuario.
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
public class UserRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ingresar un correo válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$",
            message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial"
    )
    private String password;


    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(
            regexp = "^3[0-9]{9}$",
            message = "Debe ser un número colombiano válido (ej: 3001234567)"
    )
    private String phone;

    /** Token generado por reCAPTCHA v2 en el formulario de registro. */
    @NotBlank(message = "El token de reCAPTCHA es obligatorio")
    private String recaptchaToken;
}