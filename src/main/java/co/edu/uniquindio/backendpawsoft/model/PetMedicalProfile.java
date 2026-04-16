package co.edu.uniquindio.backendpawsoft.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Hoja médica maestra de la mascota.
 * Registro único por mascota que se actualiza acumulativamente con cada atención.
 * Contiene: alergias, condiciones crónicas, medicamentos actuales, antecedentes quirúrgicos.
 */
@Entity
@Table(name = "pet_medical_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetMedicalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "pet_id", nullable = false, unique = true)
    private Pet pet;

    /**
     * Alergias conocidas acumuladas entre consultas.
     * Se concatenan, no se reemplazan.
     */
    @Column(columnDefinition = "TEXT")
    private String knownAllergies;

    /**
     * Condiciones crónicas acumuladas.
     * Se concatenan, no se reemplazan.
     */
    @Column(columnDefinition = "TEXT")
    private String chronicConditions;

    /**
     * Antecedentes quirúrgicos acumulados.
     * Se concatenan, no se reemplazan.
     */
    @Column(columnDefinition = "TEXT")
    private String surgicalHistory;

    /**
     * Medicamentos actuales.
     * Se REEMPLAZA en cada consulta (porque cambian).
     */
    @Column(columnDefinition = "TEXT")
    private String currentMedications;

    /**
     * Tipo de sangre (si aplica).
     */
    @Column(length = 10)
    private String bloodType;

    /**
     * Veterinario que hizo la última actualización.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by")
    private User lastUpdatedBy;

    /**
     * Fecha y hora de la última actualización.
     */
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    /**
     * Fecha de creación del perfil.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
