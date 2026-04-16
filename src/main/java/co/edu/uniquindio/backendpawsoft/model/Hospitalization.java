package co.edu.uniquindio.backendpawsoft.model;

import co.edu.uniquindio.backendpawsoft.enums.HospitalizationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Registro de hospitalización de una mascota.
 * Controla: admisión, estado, alta, fallecimiento y costo por hora.
 */
@Entity
@Table(name = "hospitalizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hospitalization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    /**
     * Veterinario responsable de la hospitalización.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id", nullable = false)
    private User vet;

    /**
     * Cita asociada (opcional, puede no haber cita previa).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    /**
     * Estados posibles: EN_OBSERVACION, HOSPITALIZADO, DADO_DE_ALTA, FALLECIDO
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HospitalizationStatus status;

    /**
     * Fecha y hora de admisión.
     */
    @Column(name = "admission_date", nullable = false)
    private LocalDateTime admissionDate;

    /**
     * Fecha y hora de alta (null si aún está hospitalizado).
     */
    @Column(name = "discharge_date")
    private LocalDateTime dischargeDate;

    /**
     * Motivo de la hospitalización.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    /**
     * Observaciones iniciales al momento de admisión.
     */
    @Column(columnDefinition = "TEXT")
    private String initialObservations;

    /**
     * Tarifa por hora de hospitalización.
     */
    @Column(name = "hourly_rate", nullable = false)
    private BigDecimal hourlyRate;

    /**
     * Causa del fallecimiento (solo si status = FALLECIDO).
     */
    @Column(columnDefinition = "TEXT")
    private String causeOfDeath;

    /**
     * Notas de evolución durante la hospitalización.
     */
    @OneToMany(mappedBy = "hospitalization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HospitalizationNote> notes;

    /**
     * Fecha de creación del registro.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = HospitalizationStatus.ACTIVE;
        }
    }

    /**
     * Calcula el total de horas hospitalizadas.
     */
    public Long getTotalHours() {
        if (dischargeDate == null) {
            return null; // Aún está hospitalizado
        }
        return java.time.temporal.ChronoUnit.HOURS.between(admissionDate, dischargeDate);
    }

    /**
     * Calcula el costo total de la hospitalización.
     */
    public BigDecimal getTotalCost() {
        Long hours = getTotalHours();
        if (hours == null) {
            return null;
        }
        return hourlyRate.multiply(BigDecimal.valueOf(hours));
    }
}
