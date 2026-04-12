package co.edu.uniquindio.backendpawsoft.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Ítem individual de un pago (servicio, medicamento o vacuna).
 * Permite facturación detallada.
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 */
@Entity
@Table(name = "payment_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    /** Tipo: "SERVICE", "MEDICATION", "VACCINE" */
    @Column(nullable = false, length = 20)
    private String itemType;

    /** Nombre del ítem al momento del cobro */
    @Column(nullable = false, length = 100)
    private String itemName;

    @Column(length = 255)
    private String description;

    /** Cantidad (ej: 2 dosis, 3 ml) */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    /** Unidad (ej: "dosis", "ml", "tableta") */
    @Column(length = 50)
    private String unit;

    /** Precio unitario */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /** Subtotal = quantity * unitPrice */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
