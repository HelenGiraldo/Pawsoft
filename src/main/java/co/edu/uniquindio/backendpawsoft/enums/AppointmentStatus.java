package co.edu.uniquindio.backendpawsoft.enums;

/**
 * Enumeración que representa los posibles estados de una cita médica
 * dentro del sistema Pawsoft.
 *
 * Ciclo de vida:
 *   UPCOMING → CONFIRMED → COMPLETED
 *                        → NO_SHOW
 *   Cualquier estado activo → CANCELLED
 *
 * UPCOMING  : cita recién agendada por el cliente o recepcionista.
 * CONFIRMED : cita confirmada por el recepcionista.
 * NO_SHOW   : el cliente no se presentó (marcado por recepcionista).
 * CANCELLED : cita cancelada (cliente o recepcionista).
 * COMPLETED : cita atendida y finalizada.
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

    /** El cliente no se presentó a la cita. */
    NO_SHOW,

    /** Cita cancelada. */
    CANCELLED,

    /** Cita completada satisfactoriamente. */
    COMPLETED
}