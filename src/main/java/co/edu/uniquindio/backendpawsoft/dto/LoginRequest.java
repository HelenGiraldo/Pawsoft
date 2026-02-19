package co.edu.uniquindio.backendpawsoft.dto;


import co.edu.uniquindio.backendpawsoft.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO encargado de transportar las credenciales
 * necesarias para el proceso de autenticación
 * de un usuario en el sistema.
 *
 * Esta clase recibe la información enviada desde el cliente
 * (correo electrónico y contraseña) y aplica validaciones
 * básicas antes de que el controlador procese la solicitud.
 *
 * Las validaciones se ejecutan automáticamente cuando el
 * objeto es utilizado junto con la anotación @Valid
 * en el controlador.
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


    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe de ingresar un correo válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;


}
