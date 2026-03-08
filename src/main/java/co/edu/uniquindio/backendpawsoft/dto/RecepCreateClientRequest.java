package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO para crear un cliente desde el panel de recepcionista.
 *
 * La contraseña se genera automáticamente en el backend
 * y se envía al correo del cliente — no se recibe desde el frontend.
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
public class RecepCreateClientRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El correo es obligatorio")
    @Email
    private String email;

    @Pattern(
            regexp = "^3[0-9]{9}$",
            message = "Teléfono inválido"
    )
    private String phone; // opcional, sin @NotBlank
}