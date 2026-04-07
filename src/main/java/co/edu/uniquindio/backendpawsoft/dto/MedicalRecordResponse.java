package co.edu.uniquindio.backendpawsoft.dto;

import co.edu.uniquindio.backendpawsoft.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO de respuesta para un registro médico.
 * Incluye datos de la cita, mascota y veterinario para el frontend.
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 */
public record MedicalRecordResponse(

        Long id,

        // Cita
        Long appointmentId,
        LocalDate appointmentDate,
        LocalTime appointmentTime,
        String appointmentReason,
        AppointmentStatus appointmentStatus,

        // Mascota
        Long petId,
        String petName,
        String petSpecies,
        String petBreed,
        String petBirthDate,
        String petSex,
        String petPhotoUrl,

        // Propietario
        String ownerName,
        String ownerEmail,

        // Veterinario
        String vetName,

        // Examen físico
        Double peso,
        Double temperatura,
        Integer frecuenciaCardiaca,
        Integer frecuenciaRespiratoria,
        String observacionesGenerales,

        // Diagnóstico
        String diagnosticoPrincipal,
        String diagnosticoSecundario,
        String notasClinicas,

        // Tratamiento (interno — solo vet)
        String medicamentos,
        String indicaciones,

        // Resumen para el cliente
        String diagnosticoCliente,
        String medicamentosRecetados,
        String indicacionesCliente,

        // Vacunas y controles
        String vacunasAplicadas,
        LocalDate proximoControlFecha,
        String proximoControlMotivo,

        // Archivos adjuntos
        String fotosAdjuntas,

        // Auditoría
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {}
