package co.edu.uniquindio.backendpawsoft.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Nota de evolución durante una hospitalización.
 * Registra el seguimiento diario/horario del paciente.
 */
@Entity
@Table(name = "hospitalization_notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalizationNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospitalization_id", nullable = false)
    private Hospitalization hospitalization;

    /**
     * Veterinario que escribió la nota.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id", nullable = false)
    private User vet;

    /**
     * Contenido de la nota de evolución.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String note;

    /**
     * Fecha y hora de creación de la nota.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
