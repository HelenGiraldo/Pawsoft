package co.edu.uniquindio.backendpawsoft.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro de ajustes manuales al monto de un pago.
 * Incluye auditoría completa (quién, cuándo, por qué).
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 */
@Entity
@Table(name = "payment_adjustments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    /** Monto original antes del ajuste */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal originalAmount;

    /** Monto ajustado */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal adjustedAmount;

    /** Diferencia (puede ser positiva o negativa) */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal difference;

    /** Motivo del ajuste (obligatorio) */
    @Column(nullable = false, length = 500)
    private String reason;

    /** Email de la recepcionista que hizo el ajuste */
    @Column(nullable = false, length = 120)
    private String adjustedBy;

    /** Nombre de la recepcionista */
    @Column(nullable = false, length = 120)
    private String adjustedByName;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime adjustedAt = LocalDateTime.now();
}
