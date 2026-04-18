package co.edu.uniquindio.backendpawsoft.model;

import co.edu.uniquindio.backendpawsoft.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad que representa una cita médica dentro del sistema Pawsoft.
 *
 * Una cita:
 * - Está asociada a un cliente autenticado.
 * - Se agenda para una fecha y hora específicas.
 * - Posee un estado que indica su ciclo de vida.
 *
 * Se define una restricción única sobre fecha y hora
 * para evitar solapamiento en la agenda del sistema.
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
@Entity
@Table(name = "appointments",
       indexes = {
           @Index(name = "idx_active_appointments", 
                  columnList = "date, time, vet_id, status", 
                  unique = false)
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    /**
     * Identificador único de la cita.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Fecha programada para la cita.
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Hora programada para la cita.
     */
    @Column(nullable = false)
    private LocalTime time;

    /**
     * Motivo de la cita registrado por el cliente.
     */
    @Column(nullable = false, length = 255)
    private String reason;

    /**
     * Estado actual de la cita dentro del flujo del sistema.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    /**
     * Cliente que agenda la cita.
     * Relación muchos a uno con la entidad User.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    /**
     * Veterinario asignado a la cita.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id", nullable = false)
    private User vet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;


    /** Notas adicionales registradas por el recepcionista. */
    @Column(length = 500)
    private String notes;

    /**
     * Motivo de cancelación registrado por el recepcionista.
     * Solo aplica cuando el estado es CANCELADA o CANCELLED.
     */
    @Column(length = 300)
    private String cancelReason;
}