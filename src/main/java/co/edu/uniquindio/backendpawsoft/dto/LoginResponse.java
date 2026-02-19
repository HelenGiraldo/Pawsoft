package co.edu.uniquindio.backendpawsoft.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO encargado de representar la respuesta enviada
 * por el sistema tras un proceso de autenticación exitoso.
 *
 * Esta clase encapsula la información básica que se devuelve
 * al cliente cuando las credenciales son válidas.
 * Permite estructurar la respuesta en formato JSON,
 * siguiendo buenas prácticas de diseño en APIs REST.
 *
 *
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
     * Mensaje informativo que indica el resultado
     * del proceso de autenticación.
     */
    private String message;

    /**
     * Correo electrónico del usuario autenticado.
     * Permite identificar qué cuenta inició sesión.
     */
    private String email;

    /**
     *
     */
    private String token;
}
