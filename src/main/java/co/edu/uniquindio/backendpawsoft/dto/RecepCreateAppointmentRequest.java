package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para que el recepcionista cree una cita especificando
 * el cliente por email (puede ser uno existente o recién registrado).
 *
 * A diferencia del flujo del cliente, aquí el recepcionista
 * indica explícitamente el email del cliente propietario.
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
public record RecepCreateAppointmentRequest(

        /** Email del cliente dueño de la mascota. */
        @NotBlank
        String clientEmail,

        /** ID de la mascota. */
        @NotNull
        Long petId,

        /** ID del veterinario asignado. */
        @NotNull
        Long vetId,

        /** Fecha de la cita en formato YYYY-MM-DD. */
        @NotBlank
        String date,

        /** Hora de la cita en formato HH:mm. */
        @NotBlank
        String time,

        /** Motivo de consulta. */
        @NotBlank
        String reason,

        /** Notas adicionales opcionales. */
        String notes
) {}