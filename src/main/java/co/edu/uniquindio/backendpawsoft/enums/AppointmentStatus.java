package co.edu.uniquindio.backendpawsoft.enums;

/**
 * Enumeración que representa los posibles estados de una cita médica
 * dentro del sistema Pawsoft.
 *
 * Ciclo de vida:
 *   UPCOMING → CONFIRMED → IN_PROGRESS → COMPLETED
 *                                      → NO_SHOW
 *   Cualquier estado activo → CANCELLED
 *
 * UPCOMING    : cita recién agendada por el cliente o recepcionista.
 * CONFIRMED   : cita confirmada por el recepcionista.
 * IN_PROGRESS : cita en atención activa por el veterinario.
 * NO_SHOW     : el cliente no se presentó (marcado por recepcionista).
 * CANCELLED   : cita cancelada (cliente o recepcionista).
 * COMPLETED   : cita atendida y finalizada.
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
public enum AppointmentStatus {

    /** Cita programada, pendiente de confirmación. */
    UPCOMING,

    /** Cita confirmada por el recepcionista. */
    CONFIRMED,

    /** Cita en atención activa por el veterinario. */
    IN_PROGRESS,

    /** El cliente no se presentó a la cita. */
    NO_SHOW,

    /** Cita cancelada. */
    CANCELLED,

    /** Cita completada satisfactoriamente. */
    COMPLETED
}