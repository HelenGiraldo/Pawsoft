package co.edu.uniquindio.backendpawsoft.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad que representa la tabla de precios base por tipo de servicio veterinario.
 *
 * El administrador gestiona estos valores desde su panel.
 * La recepcionista puede ajustar el monto final al registrar el cobro.
 *
 * El campo {@code serviceType} debe coincidir con los valores del
 * campo {@code reason} de la entidad {@code Appointment}
 * (ej: "Consulta general", "Vacunación", "Cirugía", etc.)
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
@Table(name = "service_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Clave única que identifica el tipo de servicio.
     * Coincide con el campo reason de Appointment.
     */
    @Column(name = "service_type", unique = true, nullable = false, length = 100)
    private String serviceType;

    /** Nombre legible para mostrar en la UI */
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /** Precio base en pesos colombianos (COP) */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Descripción opcional del servicio */
    @Column(length = 255)
    private String description;

    /**
     * Si false, no aparece en el wizard de pagos de la recepcionista.
     * El admin puede desactivar servicios sin eliminarlos.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}