package co.edu.uniquindio.backendpawsoft.dto;

/**
 * DTO de respuesta con los datos de un pago, incluyendo instantánea de la cita
 * asociada (cliente, mascota, veterinario, fecha/hora) y estado del cobro.
 *
 * Proyecto: Pawsoft
 * Universidad del Quindío — Ingeniería de Sistemas y Computación — Software III
 * Autoras: Valentina Porras Salazar · Helen Xiomara Giraldo Libreros
 * Profesor: Raúl Yulbraynner Rivera Gálvez
 */
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class PaymentResponse {

    private Long          id;
    private Long          appointmentId;

    /* ── Instantánea de la cita ── */
    private String        clientName;
    private String        clientEmail;
    private String        petName;
    private String        vetName;
    private LocalDate     appointmentDate;
    private LocalTime     appointmentTime;

    /* ── Datos del pago ── */
    private String        concept;
    private BigDecimal    baseAmount;
    private BigDecimal    amount;

    /** "PENDING" o "PAID" */
    private String        status;

    /** Fecha/hora del cobro efectivo — null si PENDING */
    private LocalDateTime paymentDate;

    private String        receivedBy;
    private String        receivedByName;
    private String        notes;
    private LocalDateTime createdAt;

    /** Ítems detallados del pago */
    private List<PaymentItemResponse> items;

    /** Historial de ajustes */
    private List<PaymentAdjustmentResponse> adjustments;
}