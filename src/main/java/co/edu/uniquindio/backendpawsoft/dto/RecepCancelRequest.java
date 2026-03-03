package co.edu.uniquindio.backendpawsoft.dto;

/**
 * DTO para cancelar una cita desde el panel del recepcionista.
 * Incluye un motivo opcional de cancelación.
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
public record RecepCancelRequest(
        /** Motivo de cancelación. Puede ser null si no se especifica. */
        String cancelReason
) {}