package co.edu.uniquindio.backendpawsoft.model;

import co.edu.uniquindio.backendpawsoft.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad que representa un registro de pago en efectivo asociado a una cita veterinaria.
 *
 * DISEÑO CLAVE — Instantánea de datos:
 * Se almacena una copia de los datos relevantes de la cita en el momento
 * del cobro. Esto garantiza que el historial financiero sea inmutable
 * aunque la cita sea editada, cancelada o eliminada en el futuro.
 *
 * REGLA DE NEGOCIO:
 * Solo puede existir UN pago por cita (unicidad por appointmentId).
 * Si la cita se elimina, el pago queda huérfano (appointmentId = null)
 * pero NUNCA se borra — es un registro contable permanente.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío
 * Programa: Ingeniería de Sistemas y Computación
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
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_appointment", columnNames = "appointment_id")
        },
        indexes = {
                @Index(name = "idx_payment_appointment", columnList = "appointment_id"),
                @Index(name = "idx_payment_client_email", columnList = "client_email"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_appointment_date", columnList = "appointment_date")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK a la cita. Nullable para que si se elimina la cita
     * el pago no sea eliminado en cascada.
     */
    @Column(name = "appointment_id")
    private Long appointmentId;

    /* ── Instantánea de la cita al momento del cobro ─────────────── */

    @Column(name = "client_name", length = 120)
    private String clientName;

    @Column(name = "client_email", length = 120)
    private String clientEmail;

    @Column(name = "pet_name", length = 80)
    private String petName;

    @Column(name = "vet_name", length = 120)
    private String vetName;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "appointment_time")
    private LocalTime appointmentTime;

    /* ── Datos del pago ──────────────────────────────────────────── */

    /** Concepto cobrado, ej: "Consulta general", "Vacunación" */
    @Column(nullable = false, length = 100)
    private String concept;

    /** Precio base tomado de la tabla service_prices al momento del cobro */
    @Column(name = "base_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseAmount;

    /**
     * Monto final cobrado al cliente.
     * Puede diferir del baseAmount si la recepcionista aplicó
     * un ajuste (descuento o recargo).
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * Fecha y hora exacta en que la recepcionista confirmó el cobro.
     * Null mientras el pago esté en PENDING.
     */
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    /** Email de la recepcionista que registró / cobró el pago */
    @Column(name = "received_by", length = 120)
    private String receivedBy;

    /** Notas adicionales (ej: "Se aplicó descuento del 10%") */
    @Column(length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Ítems detallados del pago (servicios, medicamentos, vacunas) */
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<PaymentItem> items = new java.util.ArrayList<>();

    /** Historial de ajustes manuales al monto */
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<PaymentAdjustment> adjustments = new java.util.ArrayList<>();
}