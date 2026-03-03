package co.edu.uniquindio.backendpawsoft.dto;

import co.edu.uniquindio.backendpawsoft.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO de respuesta que representa una cita médica
 * expuesta hacia el frontend.
 *
 * Este objeto permite desacoplar la entidad JPA del
 * contrato de salida de la API, garantizando control
 * sobre la información que se retorna al cliente.
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
public record AppointmentResponse(

        /**
         * Identificador único de la cita.
         */
        Long id,

        /**
         * Fecha programada.
         */
        LocalDate date,

        /**
         * Hora programada.
         */
        LocalTime time,

        /**
         * Motivo registrado por el cliente.
         */
        String reason,

        /**
         * Estado actual de la cita.
         */
        AppointmentStatus status,


        String petName,

        String vetName,

        String petPhotoUrl

) {}