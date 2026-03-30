package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO utilizado para registrar una nueva cita médica dentro del sistema Pawsoft.
 *
 * Este objeto representa la información mínima requerida para agendar
 * una cita asociada a una mascota y a un cliente autenticado.
 *
 * Incluye validaciones de integridad para garantizar:
 * - Fecha futura
 * - Hora obligatoria
 * - Motivo con longitud controlada
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
public record CreateAppointmentRequest(

        /**
         * Identificador de la mascota para la cual se agenda la cita.
         */
        @NotNull
        Long petId,

        /**
         * Fecha programada para la cita.
         * Debe ser una fecha futura.
         */
        @NotNull
        @FutureOrPresent
        LocalDate date,

        /**
         * Hora programada para la cita.
         */
        @NotNull
        LocalTime time,


        @NotNull
        Long vetId,

        /**
         * Motivo o razón de la cita.
         * Se limita entre 5 y 255 caracteres.
         */
        @NotNull
        @Size(min = 5, max = 255)
        String reason

) {}