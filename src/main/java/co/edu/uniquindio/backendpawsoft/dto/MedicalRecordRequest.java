package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO para crear o actualizar un registro médico.
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 */
public record MedicalRecordRequest(

        @NotNull Long appointmentId,

        // Examen físico
        Double peso,
        Double temperatura,
        Integer frecuenciaCardiaca,
        String observacionesGenerales,

        // Diagnóstico
        String diagnosticoPrincipal,
        String diagnosticoSecundario,
        String notasClinicas,

        // Tratamiento (interno — solo vet)
        String medicamentos,       // JSON string de medicamentos del procedimiento
        String indicaciones,

        // Resumen para el cliente
        String diagnosticoCliente,
        String medicamentosRecetados,  // JSON string de medicamentos para casa
        String indicacionesCliente,

        // Vacunas y controles
        String vacunasAplicadas,   // JSON string de vacunas aplicadas
        LocalDate proximoControlFecha,
        String proximoControlMotivo,

        // Archivos adjuntos
        String fotosAdjuntas       // JSON string de URLs de fotos (radiografías, etc.)
) {}
