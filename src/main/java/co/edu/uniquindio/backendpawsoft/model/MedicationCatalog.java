package co.edu.uniquindio.backendpawsoft.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Catálogo de medicamentos con precios.
 * Gestionado por el administrador.
 *
 * Proyecto: Pawsoft — Software III
 * Autoras: Valentina Porras · Helen Xiomara Giraldo
 */
@Entity
@Table(name = "medication_catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    /** Precio unitario en COP */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Unidad de medida (ej: "ml", "tableta", "ampolla") */
    @Column(length = 50)
    private String unit;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
