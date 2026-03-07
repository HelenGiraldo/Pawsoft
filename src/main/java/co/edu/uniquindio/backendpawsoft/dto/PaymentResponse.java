package co.edu.uniquindio.backendpawsoft.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    private String        notes;
    private LocalDateTime createdAt;
}