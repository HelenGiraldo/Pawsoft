package co.edu.uniquindio.backendpawsoft.dto;

import co.edu.uniquindio.backendpawsoft.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO de respuesta enriquecido para el panel del recepcionista.
 *
 * Incluye datos completos del cliente, mascota y veterinario
 * para poder renderizar la tabla de citas sin llamadas adicionales.
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
public record RecepAppointmentResponse(

        /** Identificador único de la cita. */
        Long id,

        /** Fecha programada. */
        LocalDate date,

        /** Hora programada. */
        LocalTime time,

        /** Motivo de consulta. */
        String reason,

        /** Estado actual de la cita. */
        AppointmentStatus status,

        /** Motivo de cancelación, si aplica. */
        String cancelReason,

        /* ── Cliente ── */
        Long clientId,
        String clientName,
        String clientEmail,

        /* ── Mascota ── */
        Long petId,
        String petName,
        String petSpecies,
        String petPhotoUrl,
        String petBreed,
        String petBirthday,

        /* ── Veterinario ── */
        Long vetId,
        String vetName,
        String vetPhotoUrl,

         String notes
) {}