package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para que el recepcionista edite una cita existente.
 * Permite cambiar fecha, hora, veterinario y motivo.
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
public record RecepUpdateAppointmentRequest(

        @NotNull LocalDate date,
        @NotNull LocalTime time,
        @NotNull Long vetId,
        String reason,
        String notes
) {}