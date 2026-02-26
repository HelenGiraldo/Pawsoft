package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Data;

/**
 * DTO que representa la solicitud de restablecimiento de contraseña en Pawsoft.
 *
 * <p>Este objeto se usa cuando un usuario indica que olvidó su contraseña y necesita
 * iniciar el flujo de recuperación. Usualmente se envía desde el cliente a un endpoint
 * del módulo de autenticación (por ejemplo, {@code /auth/password-reset/request}).</p>
 *
 * <h3>Uso típico del flujo</h3>
 * <ul>
 *   <li>El usuario ingresa su correo.</li>
 *   <li>El backend valida el formato y procesa la solicitud.</li>
 *   <li>Si aplica, el backend genera un token/código y lo envía al correo.</li>
 * </ul>
 *
 * <h3>Notas de seguridad</h3>
 * <ul>
 *   <li>
 *     Se recomienda responder de forma genérica para no revelar si el correo existe
 *     en el sistema (prevención de enumeración de usuarios).
 *   </li>
 *   <li>
 *     El correo se considera un identificador del usuario (ver entidad {@code User}),
 *     por lo que debe manejarse con validaciones y controles apropiados.
 *   </li>
 * </ul>
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
public class RequestPasswordReset {

    /**
     * Correo electrónico del usuario que solicita el restablecimiento.
     *
     * <p>Corresponde al correo registrado en el sistema. Idealmente debe validarse
     * con reglas de formato (por ejemplo, {@code @Email}) y no debe venir vacío.</p>
     */
    private String email;
}