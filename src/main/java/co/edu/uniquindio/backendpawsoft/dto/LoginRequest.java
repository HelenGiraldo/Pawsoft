package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO encargado de transportar las credenciales necesarias para el proceso de autenticación
 * de un usuario en el sistema.
 *
 * Esta clase recibe la información enviada desde el cliente (correo electrónico y contraseña)
 * y aplica validaciones básicas antes de que el controlador procese la solicitud.
 *
 * Las validaciones se ejecutan automáticamente cuando el objeto es utilizado junto con
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
@Data
public class LoginRequest {

    /**
     * Correo electrónico del usuario.
     * Es obligatorio y debe tener un formato válido.
     */
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe de ingresar un correo válido")
    private String email;

    /**
     * Contraseña del usuario.
     * Es obligatoria.
     */
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;


    /** Token generado por reCAPTCHA v2 en el frontend. */
    @NotBlank(message = "El token de reCAPTCHA es obligatorio")
    private String recaptchaToken;
}