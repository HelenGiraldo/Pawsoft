package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO para la creación o actualización de un usuario.
 *
 * Contiene los datos mínimos requeridos para registrar o modificar un usuario
 * y define validaciones para garantizar integridad de la información y contraseñas seguras.
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

    /**
     * Nombre completo del usuario.
     * Es obligatorio.
     */
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    /**
     * Correo electrónico del usuario.
     * Es obligatorio y debe tener formato válido.
     */
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ingresar un correo válido")
    private String email;

    /**
     * Contraseña del usuario.
     *
     * Reglas:
     * - mínimo 8 caracteres
     * - al menos una letra mayúscula
     * - al menos un número
     * - al menos un carácter especial
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial"
    )
    private String password;
}