package co.edu.uniquindio.backendpawsoft.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO que representa el cuerpo de la petición para registrar un pago.
 *
 * Incluye una instantánea de los datos de la cita para que
 * el historial de pagos sea autónomo e inmutable.
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
@Data
public class PaymentRequest {

    @NotNull(message = "El ID de la cita es obligatorio")
    private Long appointmentId;

    /* ── Instantánea de la cita ── */

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String clientName;

    @NotBlank @Email
    private String clientEmail;

    @NotBlank(message = "El nombre de la mascota es obligatorio")
    private String petName;

    @NotBlank(message = "El nombre del veterinario es obligatorio")
    private String vetName;

    /** Formato YYYY-MM-DD */
    @NotBlank
    private String appointmentDate;

    /** Formato HH:mm */
    @NotBlank
    private String appointmentTime;

    /* ── Datos del pago ── */

    @NotBlank(message = "El concepto es obligatorio")
    private String concept;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal baseAmount;

    /** Monto final — puede diferir del base si la recepcionista lo ajustó */
    @NotNull
    @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
    private BigDecimal amount;

    private String notes;
}