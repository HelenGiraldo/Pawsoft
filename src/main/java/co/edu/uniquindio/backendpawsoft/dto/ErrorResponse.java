package co.edu.uniquindio.backendpawsoft.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO que representa la estructura estándar de respuesta
 * ante errores generados en la API REST.
 *
 *
 * Esta clase permite encapsular información relevante sobre
 * errores ocurridos durante el procesamiento de una solicitud,
 * proporcionando un formato uniforme para el cliente.
 *
 *
 *
 * Incluye el código de estado HTTP, un mensaje descriptivo
 * y la fecha y hora en que ocurrió el error.
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
@NoArgsConstructor
public class ErrorResponse {

    /**
     * Código de estado HTTP asociado al error.
     */
    private int status;

    /**
     * Mensaje descriptivo que explica la causa del error.
     */
    private String message;

    /**
     * Fecha y hora en que ocurrió el error.
     */
    private LocalDateTime timestamp;

}
