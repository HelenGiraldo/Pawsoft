package co.edu.uniquindio.backendpawsoft.dto;

/**
 * DTO para la verificación de autenticación de dos factores (2FA).
 * Contiene el email del usuario y el código de verificación recibido.
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
 * - Raúl Yulbraynner Rivera Gálvez
 */
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la verificación de códigos de autenticación de dos factores (2FA).
 * 
 * Contiene el email del usuario y el código de verificación recibido
 * para completar el proceso de autenticación.
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorVerifyRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "El código de verificación es obligatorio")
    @Pattern(
        regexp = "^[0-9]{6}$",
        message = "El código debe tener exactamente 6 dígitos"
    )
    private String code;
}